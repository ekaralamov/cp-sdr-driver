package sdr.driver.cp.test

import io.mockk.MockKMatcherScope
import io.mockk.coEvery
import io.mockk.every
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

sealed class AnswerPrompter<T> {

    enum class CallState {
        Answered,
        Waiting,
        Cancelled,
        Failed
    }

    protected sealed class Prompt<T> {
        class Value<T>(val value: T) : Prompt<T>()
        class Throwable<T>(val throwable: kotlin.Throwable) : Prompt<T>()
    }

    private class Coroutine<T>(stubBlock: suspend MockKMatcherScope.() -> T) : AnswerPrompter<T>() {

        private val channel = Channel<Prompt<T>>()

        init {
            coEvery(stubBlock).coAnswers {
                lastCallState = CallState.Waiting
                val prompt: Prompt<T>
                try {
                    prompt = channel.receive()
                } catch (cancellationException: CancellationException) {
                    lastCallState = CallState.Cancelled
                    throw cancellationException
                } catch (throwable: Throwable) {
                    lastCallState = CallState.Failed
                    throw throwable
                }
                lastCallState = CallState.Answered
                when (prompt) {
                    is Prompt.Value -> prompt.value
                    is Prompt.Throwable -> throw prompt.throwable
                }
            }
        }

        override fun prompt(prompt: Prompt<T>) = apply {
            channel.offer(prompt) || throw AssertionError("call not made")
        }

        override fun thatsIt() {
            channel.close(AssertionError("unexpected call"))
        }
    }

    private class Blocking<T>(stubBlock: MockKMatcherScope.() -> T) : AnswerPrompter<T>() {

        private val lock = ReentrantLock()
        private var promptBell: Condition? = lock.newCondition()
        private var prompt: Prompt<T>? = null

        init {
            every(stubBlock).answers {
                lastCallState = CallState.Waiting
                lock.withLock {
                    while (true) {
                        if (prompt != null) break
                        if (promptBell == null) {
                            lastCallState = CallState.Failed
                            throw AssertionError("unexpected call")
                        }
                        promptBell!!.await()
                    }
                    lastCallState = CallState.Answered
                    val p = prompt!!
                    prompt = null
                    promptBell?.signal()
                    when (p) {
                        is Prompt.Value -> return@answers p.value
                        is Prompt.Throwable -> throw p.throwable
                    }
                }
            }
        }

        override fun prompt(prompt: Prompt<T>) = apply {
            lock.withLock {
                while (this.prompt != null) {
                    try {
                        if (!promptBell!!.await(10, TimeUnit.MILLISECONDS))
                            throw AssertionError("call not made")
                    } catch (interruptedException: InterruptedException) {
                    }
                }
                this.prompt = prompt
                promptBell!!.signal()
            }
        }

        override fun thatsIt() {
            lock.withLock {
                val pb = promptBell!!
                promptBell = null
                pb.signal()
            }
        }
    }

    var lastCallState: CallState? = null
        protected set

    fun prompt(value: T) = prompt(Prompt.Value(value))

    fun prompt(throwable: Throwable) = prompt(Prompt.Throwable(throwable))

    protected abstract fun prompt(prompt: Prompt<T>): AnswerPrompter<T>

    abstract fun thatsIt()

    companion object {
        fun <T> ofSuspend(stubBlock: suspend MockKMatcherScope.() -> T): AnswerPrompter<T> = Coroutine(stubBlock)
        fun <T> of(stubBlock: MockKMatcherScope.() -> T): AnswerPrompter<T> = Blocking(stubBlock)
    }
}
