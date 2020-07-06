package sdr.driver.cp.test

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class FailOnThreadExceptionRule : TestRule {

    private val sump = Sump()

    override fun apply(base: Statement, description: Description) = object : Statement() {
        override fun evaluate() {
            sump.install()
            try {
                base.evaluate()
                sump.dump()
            } finally {
                sump.uninstall()
            }
        }
    }
}
