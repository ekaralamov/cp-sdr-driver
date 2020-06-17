package app.ekaralamov.test

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

class FlowTestCollector<T> internal constructor(flow: Flow<T>, coroutineContext: CoroutineContext) {

    private val _values = ArrayList<T>()
    var error: Throwable? = null
        private set
    val values: List<T> = _values

    private val job = GlobalScope.launch(TestDispatcher + coroutineContext[Job]!!) {
        try {
            flow.collect { _values += it }
        } catch (throwable: Throwable) {
            error = throwable
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
        job.cancel()
    }
}

suspend fun <T> Flow<T>.test() = FlowTestCollector<T>(this, coroutineContext)
