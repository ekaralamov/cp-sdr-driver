package app.ekaralamov

import app.ekaralamov.test.CoroutineTestContainer
import kotlinx.coroutines.CoroutineScope
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class CoroutineTestRule : TestRule {

    private val containersList = ArrayList<CoroutineTestContainer<*>>()

    fun <T> run(block: suspend CoroutineScope.() -> T): CoroutineTestContainer<T> =
        CoroutineTestContainer.runOrphan(block).also { containersList.add(it) }

    override fun apply(base: Statement, description: Description) = object : Statement() {
        override fun evaluate() {
            try {
                base.evaluate()
            } finally {
                containersList.forEach { it.close() }
            }
        }
    }
}
