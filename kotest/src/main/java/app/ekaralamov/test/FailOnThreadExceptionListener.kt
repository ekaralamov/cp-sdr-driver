package app.ekaralamov.test

import io.kotest.core.listeners.TestListener
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import io.kotest.core.test.TestStatus

class FailOnThreadExceptionListener : TestListener {

    private val sump = Sump()
    private var depth = 0

    override suspend fun beforeTest(testCase: TestCase) {
        if (depth++ == 0)
            sump.install()
    }

    override suspend fun afterTest(testCase: TestCase, result: TestResult) {
        if (result.status == TestStatus.Success)
            sump.dump()
        if (--depth == 0)
            sump.uninstall()
    }
}
