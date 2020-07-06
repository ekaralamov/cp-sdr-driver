package sdr.driver.cp.test

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

class ReceiveChannelTestConsumer<T> internal constructor(
    private val channel: ReceiveChannel<T>,
    coroutineContext: CoroutineContext
) {

    private val _values = ArrayList<T>()
    var error: Throwable? = null
        private set
    val values: List<T> = _values

    init {
        GlobalScope.launch(TestDispatcher + coroutineContext[Job]!!) {
            try {
                channel.consumeEach { _values += it }
            } catch (throwable: Throwable) {
                error = throwable
            }
        }
    }

    fun lastOf(expectedEmissionsCount: Int): T {
        var emissionsCount = _values.size
        if (error != null)
            emissionsCount++
        emissionsCount shouldBe expectedEmissionsCount

        error?.let { throw it }

        return _values.last()
    }

    fun close() {
        channel.cancel()
    }
}

suspend fun <T> ReceiveChannel<T>.test() =
    ReceiveChannelTestConsumer(this, coroutineContext)
