package app.ekaralamov.test

import io.mockk.MockKStubScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel

class AnswerPrompter<T> internal constructor(coEvery: MockKStubScope<T, T>) {

    enum class CallState {
        Answered,
        Waiting,
        Cancelled,
        Failed
    }

    private sealed class Prompt<T> {
        class Value<T>(val value: T) : Prompt<T>()
        class Throwable<T>(val throwable: kotlin.Throwable) : Prompt<T>()
    }

    private val channel = Channel<Prompt<T>>()

    var lastCallState: CallState? = null
        private set

    init {
        coEvery.coAnswers {
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

    fun prompt(value: T) = prompt(Prompt.Value(value))

    fun prompt(throwable: Throwable) = prompt(Prompt.Throwable(throwable))

    private fun prompt(prompt: Prompt<T>) = apply {
        channel.offer(prompt) || throw AssertionError("call not made")
    }

    fun thatsIt() {
        channel.close(AssertionError("unexpected call"))
    }
}

fun <T> answerPrompterFor(coEvery: MockKStubScope<T, T>) = AnswerPrompter(coEvery)
