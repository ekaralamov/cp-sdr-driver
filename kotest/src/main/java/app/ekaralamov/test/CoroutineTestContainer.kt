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
        supervisorJob.complete()
    }

    companion object {

        suspend fun <T> run(block: suspend CoroutineScope.() -> T): CoroutineTestContainer<T> =
            run(block, checkNotNull(coroutineContext[Job]))

        fun <T> runOrphan(block: suspend CoroutineScope.() -> T): CoroutineTestContainer<T> =
            run(block, null)

        private fun <T> run(
            block: suspend CoroutineScope.() -> T,
            parent: Job?
        ): CoroutineTestContainer<T> {
            val supervisorJob = SupervisorJob(parent)
            return CoroutineTestContainer(
                supervisorJob,
                GlobalScope.async(TestCoroutineDispatcher() + supervisorJob, block = block)
            )
        }
    }
}
