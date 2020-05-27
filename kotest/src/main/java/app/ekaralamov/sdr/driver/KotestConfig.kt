package app.ekaralamov.sdr.driver

import app.ekaralamov.test.Sump
import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.listeners.Listener
import io.kotest.core.listeners.TestListener
import io.kotest.core.spec.IsolationMode
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import io.kotest.core.test.TestStatus

object KotestConfig : AbstractProjectConfig() {

    override val isolationMode = IsolationMode.InstancePerLeaf

    override fun listeners(): List<Listener> = listOf(FailOnThreadException())
}

private class FailOnThreadException : TestListener {

    private val sump = Sump()

    override suspend fun beforeTest(testCase: TestCase) =
        sump.install()

    override suspend fun afterTest(testCase: TestCase, result: TestResult) {
        if (result.status == TestStatus.Success)
            sump.dump()
        sump.uninstall()
    }
}
