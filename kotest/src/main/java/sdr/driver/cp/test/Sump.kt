package sdr.driver.cp.test

import java.util.concurrent.atomic.AtomicReference

internal class Sump : Thread.UncaughtExceptionHandler {

    private var savedUncaughtExceptionHandler: Thread.UncaughtExceptionHandler? = null
    private val throwable = AtomicReference<Throwable>()

    fun install() {
        savedUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    fun uninstall() {
        Thread.setDefaultUncaughtExceptionHandler(savedUncaughtExceptionHandler)
        savedUncaughtExceptionHandler = null
        throwable.set(null)
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        this.throwable.compareAndSet(null, throwable)
    }

    fun dump() {
        throwable.getAndSet(null)?.let { throw it }
    }
}
