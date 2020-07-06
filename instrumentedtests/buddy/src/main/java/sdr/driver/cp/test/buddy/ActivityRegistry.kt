package sdr.driver.cp.test.buddy

import java.util.*

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
internal object ActivityRegistry {

    private class Result {

        private var outcome: Int? = null

        @Synchronized
        fun waitFor(): Int =
            outcome ?: run {
                (this as Object).wait(Timeout)
                outcome ?: InvalidActivityResult
            }

        @Synchronized
        fun set(outcome: Int) {
            this.outcome = outcome
            (this as Object).notifyAll()
        }

        companion object {
            private const val Timeout = 10_000L
            private const val InvalidActivityResult = Int.MIN_VALUE
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

    fun set(outcome: Int, key: Int) = result(key).set(outcome)

    @Synchronized
    private fun result(key: Int): Result = map.getValue(key)
}
