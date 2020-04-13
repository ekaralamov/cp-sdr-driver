package app.ekaralamov.sdr.driver.test.buddy

import java.util.*

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
internal object ActivityRegistry {

    private class Result {

        private var outcome: Boolean? = null

        @Synchronized
        fun waitFor(): Boolean =
            outcome ?: run {
                (this as Object).wait(Timeout)
                outcome ?: throw Exception("timed out waiting for activity result")
            }

        @Synchronized
        fun set(outcome: Boolean) {
            this.outcome = outcome
            (this as Object).notifyAll()
        }

        companion object {
            private const val Timeout = 10_000L
        }
    }

    private var lastKey = 0

    private val map = TreeMap<Int, Result>()

    fun newEntry(): Int {
        val result = Result()
        synchronized(this) {
            map[++lastKey] = result
            return lastKey
        }
    }

    fun waitFor(key: Int) =
        try {
            result(key).waitFor()
        } finally {
            synchronized(this) {
                map.remove(key)
            }
        }

    fun set(outcome: Boolean, key: Int) = result(key).set(outcome)

    @Synchronized
    private fun result(key: Int): Result = map.getValue(key)
}
