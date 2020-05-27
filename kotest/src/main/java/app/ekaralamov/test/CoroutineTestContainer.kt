package app.ekaralamov.test

import kotlinx.coroutines.*
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlin.coroutines.coroutineContext

class CoroutineTestContainer<T>(
    private val supervisorJob: CompletableJob,
    private val deferredResult: Deferred<T>
) {

    fun getResult() = deferredResult.getCompleted()

    val hasCompleted: Boolean
        get() = deferredResult.isCompleted

    fun cancel() = deferredResult.cancel()

    fun close() {
        cancel()
        supervisorJob.complete()
    }

    companion object {

        suspend fun <T> run(
            dispatcher: CoroutineDispatcher = TestDispatcher,
            block: suspend CoroutineScope.() -> T
        ): CoroutineTestContainer<T> =
            run(checkNotNull(coroutineContext[Job]), dispatcher, block)

        fun <T> runOrphan(block: suspend CoroutineScope.() -> T): CoroutineTestContainer<T> =
            run(null, TestDispatcher, block)

        private fun <T> run(
            parent: Job?,
            dispatcher: CoroutineDispatcher,
            block: suspend CoroutineScope.() -> T
        ): CoroutineTestContainer<T> {
            val supervisorJob = SupervisorJob(parent)
            return CoroutineTestContainer(
                supervisorJob,
                GlobalScope.async(dispatcher + supervisorJob, block = block)
            )
        }
    }
}

val TestDispatcher = TestCoroutineDispatcher()
