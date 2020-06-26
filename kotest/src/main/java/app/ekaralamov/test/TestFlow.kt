package app.ekaralamov.test

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart

class TestFlow<T> {

    private val channel = Channel<T>()

    enum class State {
        NotStarted,
        Started,
        Cancelled
    }

    var state = State.NotStarted
        private set

    val flow = channel.consumeAsFlow()
        .onStart { state = State.Started }
        .onCompletion { cause -> if (cause is CancellationException) state = State.Cancelled }

    suspend fun emit(value: T) {
        channel.send(value)
    }

    fun finishWith(throwable: Throwable) {
        channel.close(throwable)
    }
}
