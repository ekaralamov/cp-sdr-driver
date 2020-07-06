package sdr.driver.cp

import sdr.driver.cp.test.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay

class TunerAccessTokenSpec : DescribeSpec({

    describe("TunerAccessToken.Registry") {
        val permissionRepository = mockk<ClientPermissionRepository>()

        val sut = TunerAccessToken.Registry<Any, TunerAccessToken.Session>(permissionRepository)

        describe("when a token is being acquired initially") {
            val address = Any()
            val sessionFactory = mockk<(Any) -> TunerAccessToken.Session>()

            context("permission has been granted to the calling package") {
                coEvery { permissionRepository.retrieveResolutionFor("package") } returns ClientPermissionResolution.Permanent.Granted
                val sessionPrompter = AnswerPrompter.of { sessionFactory(address) }

                val testContainer = CoroutineTestContainer.run(Dispatchers.IO) {
                    sut.acquireToken(address, "package", sessionFactory)
                }
                delay(10)

                describe("when a token is being acquired again for the same address by the same package") {
                    val testContainer2 = CoroutineTestContainer.run(Dispatchers.IO) {
                        sut.acquireToken(address, "package", sessionFactory)
                    }
                    delay(10)

                    describe("when session is created") {
                        val session = mockk<TunerAccessToken.Session>()
                        sessionPrompter.prompt(session)
                        delay(10)

                        it("returns token with the session for the first request") {
                            testContainer.getResult().session shouldBe session
                        }

                        it("returns token with the session for the second request") {
                            testContainer2.getResult().session shouldBe session
                        }

                        describe("when the second token is released") {
                            testContainer2.getResult().release()

                            describe("when the first token is released") {
                                val closePrompter = AnswerPrompter.ofSuspend { session.close() }
                                val testContainer3 = CoroutineTestContainer.run { testContainer.getResult().release() }

                                it("allows for revoke permission call") {
                                    sut.revokeTokensFor("package")
                                }

                                describe("when the session is closed") {
                                    closePrompter.prompt(Unit).thatsIt()

                                    describe("when a token is being acquired again for the same address by the same package") {
                                        val session2 = mockk<TunerAccessToken.Session>()
                                        coOne { sessionFactory(address) } returns session2

                                        val token3 = sut.acquireToken(address, "package", sessionFactory)

                                        it("returns token with new session") {
                                            token3.session shouldBe session2
                                        }
                                    }
                                }
                                testContainer3.close()
                            }

                            describe("when permission is revoked") {
                                coOne { session.close() } just Runs

                                sut.revokeTokensFor("package")

                                it("closes the session") {
                                    coVerify { session.close() }
                                }

                                it("allows for releasing the first token") {
                                    testContainer.getResult().release()
                                }
                            }
                        }

                        describe("when a token is being acquired for another address by the same package") {
                            val address2 = Any()
                            val session2 = mockk<TunerAccessToken.Session>()
                            one { sessionFactory(address2) } returns session2

                            val token3 = sut.acquireToken(address2, "package", sessionFactory)

                            it("returns token with new session") {
                                token3.session shouldBe session2
                            }
                        }

                        describe("when a token is being acquired for the same address by another package") {

                            it("throws DeviceBusyException") {
                                shouldThrow<DeviceBusyException> {
                                    sut.acquireToken(address, "another package", sessionFactory)
                                }
                            }
                        }
                    }

                    describe("when session creation fails") {
                        sessionPrompter.prompt(Exception("test exception")).thatsIt()
                        delay(10)

                        it("throws exception") {
                            shouldThrowAny {
                                testContainer.getResult()
                            }
                        }
                    }
                    testContainer2.close()
                }
                testContainer.close()
            }

            forAll(
                row(null),
                row(ClientPermissionResolution.Denied),
                row(ClientPermissionResolution.Permanent.Denied)
            ) { permissionResolution ->
                context("permission resolution for the calling package is $permissionResolution") {
                    coOne { permissionRepository.retrieveResolutionFor("package") } returns permissionResolution

                    it("throws SecurityException") {
                        shouldThrow<SecurityException> {
                            sut.acquireToken(address, "package", sessionFactory)
                        }
                    }
                }
            }

            context("permission resolution for the calling package can't be retrieved") {
                coEvery { permissionRepository.retrieveResolutionFor("package") } throws Exception("test exception")

                it("throws exception") {
                    shouldThrowAny {
                        sut.acquireToken(address, "package", sessionFactory)
                    }
                }
            }
        }
    }
})
