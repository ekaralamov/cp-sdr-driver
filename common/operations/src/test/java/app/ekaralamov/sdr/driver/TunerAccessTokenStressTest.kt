package app.ekaralamov.sdr.driver

import app.ekaralamov.test.FailOnThreadExceptionRule
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.atomic.AtomicBoolean

class TunerAccessTokenStressTest {

    @get:Rule
    val failOnThreadExceptionRule = FailOnThreadExceptionRule()

    private class Session : TunerAccessToken.Session {

        private val open = AtomicBoolean()

        fun open() {
            assertThat(open.compareAndSet(false, true)).isTrue()
        }

        override suspend fun close() {
            assertThat(open.compareAndSet(true, false)).isTrue()
        }
    }

    @Test
    fun test() {
        val threadCount = 150

        val sessionFactory : (Session) -> Session = { it.apply { open() } }

        val sessions = arrayOf(Session(), Session())
        val packageNames = arrayOf("package 1", "package 2")

        val permissionRepository = mockk<ClientPermissionRepository> {
            coEvery { retrieveResolutionFor("package 1") } returns ClientPermissionResolution.Permanent.Granted
            coEvery { retrieveResolutionFor("package 2") } returns ClientPermissionResolution.Permanent.Granted
            coEvery { retrieveResolutionFor("package 3") } returns null
        }

        val sut = TunerAccessToken.Registry<Session, Session>(permissionRepository)

        val random = ThreadLocalRandom.current()

        class AuthorizedClient : Runnable {

            private val packageName = packageNames[random.nextInt(2)]
            private val session = sessions[random.nextInt(2)]

            override fun run() {
                try {
                    val token = sut.acquireToken(session, packageName, sessionFactory)
                    assertThat(token.session).isEqualTo(session)
                    runBlocking { token.release() }
                } catch (deviceBusyException: DeviceBusyException) {
                } catch (permissionRevokedException: PermissionRevokedException) {
                }
            }
        }

        class UnauthorizedClient : Runnable {

            private val session = sessions[random.nextInt(2)]

            override fun run() {
                try {
                    sut.acquireToken(session, "package 3", sessionFactory)
                    throw AssertionError("should have failed")
                } catch (deviceBusyException: DeviceBusyException) {
                } catch (securityException: SecurityException) {
                }
            }
        }

        class Revoker : Runnable {

            private val packageName = packageNames[random.nextInt(2)]

            override fun run() {
                runBlocking { sut.revokeTokensFor(packageName) }
            }
        }

        val threads = Array(threadCount) {
            Thread(
                when (random.nextInt(8)) {
                    in 0..5 -> AuthorizedClient()
                    6 -> UnauthorizedClient()
                    7 -> Revoker()
                    else -> throw AssertionError()
                }
            ).apply { start() }
        }

        threads.forEach { it.join() }
    }
}
