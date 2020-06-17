package app.ekaralamov.test

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.onStart

class TestFlow<T> {

    private val channel = Channel<T>()

    var wasStarted = false
        private set

    val flow = channel.consumeAsFlow().onStart { wasStarted = true }

    suspend fun emit(value: T) {
        channel.send(value)
    }

    fun finishWith(throwable: Throwable) {
        channel.close(throwable)
    }
}
