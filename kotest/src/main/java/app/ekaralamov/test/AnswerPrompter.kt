package app.ekaralamov.test

import io.mockk.MockKStubScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel

class AnswerPrompter<T> internal constructor(coEvery: MockKStubScope<T, T>) {

    private val channel = Channel<T>()

    var isCancelled = false
        private set

    init {
        coEvery.coAnswers {
            try {
                channel.receive()
            } catch (cancellationException: CancellationException) {
                isCancelled = true
                throw cancellationException
            }
        }
    }

    suspend fun prompt(answer: T) = channel.send(answer)

    fun prompt(throwable: Throwable) {
        channel.close(throwable)
    }
}

fun <T> answerPrompterFor(coEvery: MockKStubScope<T, T>) = AnswerPrompter(coEvery)
