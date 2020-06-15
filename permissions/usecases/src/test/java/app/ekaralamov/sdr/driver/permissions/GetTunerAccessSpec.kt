package app.ekaralamov.sdr.driver.permissions

import android.hardware.usb.UsbDevice
import app.ekaralamov.sdr.driver.ClientPermissionResolution
import app.ekaralamov.sdr.driver.ClientPermissionStorage
import app.ekaralamov.test.*
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import javax.inject.Provider

class GetTunerAccessSpec : DescribeSpec({
    describe("when GetTunerAccess is invoked") {
        val clientPermissionStorage = mockk<ClientPermissionStorage>()
        val platformDevicePermissionAuthorityProvider =
            mockk<Provider<PlatformDevicePermissionAuthority>>()

        val sut = GetTunerAccess(
            clientPermissionStorage,
            platformDevicePermissionAuthorityProvider
        )

        val device = mockk<UsbDevice>()

        val retrieveClientPermissionPrompter = AnswerPrompter.ofSuspend {
            clientPermissionStorage.retrieveResolutionFor("client package name")
        }

        val testContainer = CoroutineTestContainer.run {
            sut("client package name", device)
        }

        itCallsFor("client permission resolution retrieval", retrieveClientPermissionPrompter, testContainer)

        describe("when permission storage responds") {

            forAll(
                row("no resolution", null as ClientPermissionResolution?, false),
                row("denied", ClientPermissionResolution.Denied, true)
            ) { responseDescription,
                clientPermissionResponse,
                permanentDenialOptionPresent ->

                describe("with $responseDescription") {
                    retrieveClientPermissionPrompter.prompt(clientPermissionResponse).thatsIt()

                    val grantPermissionToClientQuestion: GetTunerAccess.Result.GrantPermissionToClientQuestion by nosynchLazy {
                        testContainer.getResult() as GetTunerAccess.Result.GrantPermissionToClientQuestion
                    }

                    if (permanentDenialOptionPresent) {
                        val never: (suspend () -> Unit) by nosynchLazy {
                            grantPermissionToClientQuestion.never!!
                        }

                        it("returns `GrantPermissionToClientQuestion` with permanent denial option") {
                            shouldNotThrowAny { never }
                        }

                        describe("when permanent denial answer is given") {
                            val storeClientPermissionPrompter = AnswerPrompter.ofSuspend {
                                clientPermissionStorage.storeResolution(
                                    "client package name",
                                    ClientPermissionResolution.Permanent.Denied
                                )
                            }

                            val testContainer = CoroutineTestContainer.run {
                                never()
                            }

                            itCallsFor(
                                "storing client permission permanently denied resolution",
                                storeClientPermissionPrompter,
                                testContainer
                            )

                            describe("when the client permission resolution is stored") {
                                storeClientPermissionPrompter.prompt(Unit).thatsIt()

                                it("completes successfully") {
                                    shouldNotThrowAny {
                                        testContainer.getResult()
                                    }
                                }
                            }

                            testContainer.close()
                        }
                    } else
                        it("returns `GrantPermissionToClientQuestion` without permanent denial option") {
                            grantPermissionToClientQuestion.never shouldBe null
                        }

                    describe("when _not_ permanent denial answer is given") {
                        val storeClientPermissionPrompter = AnswerPrompter.ofSuspend {
                            clientPermissionStorage.storeResolution(
                                "client package name",
                                ClientPermissionResolution.Denied
                            )
                        }

                        val testContainer = CoroutineTestContainer.run {
                            grantPermissionToClientQuestion.no()
                        }

                        itCallsFor(
                            "storing client permission denied resolution",
                            storeClientPermissionPrompter,
                            testContainer
                        )

                        describe("when the client permission resolution is stored") {
                            storeClientPermissionPrompter.prompt(Unit).thatsIt()

                            it("completes successfully") {
                                shouldNotThrowAny {
                                    testContainer.getResult()
                                }
                            }
                        }

                        testContainer.close()
                    }

                    describe("when grant answer is given") {
                        val storeClientPermissionPrompter = AnswerPrompter.ofSuspend {
                            clientPermissionStorage.storeResolution(
                                "client package name",
                                ClientPermissionResolution.Permanent.Granted
                            )
                        }

                        val testContainer = CoroutineTestContainer.run {
                            grantPermissionToClientQuestion.yes()
                        }

                        itCallsFor(
                            "storing client permission granted resolution",
                            storeClientPermissionPrompter,
                            testContainer
                        )

                        describe("when the client permission resolution is stored") {
                            val platformDevicePermissionAuthority =
                                mockk<PlatformDevicePermissionAuthority>()
                            one { platformDevicePermissionAuthorityProvider.get() } returns platformDevicePermissionAuthority
                            val devicePermissionPrompter = AnswerPrompter.ofSuspend {
                                platformDevicePermissionAuthority.getPermissionFor(device)
                            }

                            storeClientPermissionPrompter.prompt(Unit).thatsIt()

                            itCallsFor("obtaining device permission", devicePermissionPrompter, testContainer)

                            describe("when device permission") {
                                forAll(
                                    row("granted", true, GetTunerAccess.Result.DeviceAccess.Granted),
                                    row("denied", false, GetTunerAccess.Result.DeviceAccess.Denied)
                                ) { resolutionDesc,
                                    devicePermissionCallResult,
                                    expectedResult ->
                                    describe("is $resolutionDesc") {
                                        devicePermissionPrompter.prompt(devicePermissionCallResult).thatsIt()

                                        it("returns ${expectedResult::class}") {
                                            testContainer.getResult() shouldBe expectedResult
                                        }
                                    }
                                }
                            }
                        }

                        testContainer.close()
                    }
                }
            }

            describe("with granted") {
                val platformDevicePermissionAuthority =
                    mockk<PlatformDevicePermissionAuthority>()
                one { platformDevicePermissionAuthorityProvider.get() } returns platformDevicePermissionAuthority
                val devicePermissionPrompter = AnswerPrompter.ofSuspend {
                    platformDevicePermissionAuthority.getPermissionFor(device)
                }

                retrieveClientPermissionPrompter.prompt(ClientPermissionResolution.Permanent.Granted)
                    .thatsIt()

                itCallsFor("obtaining device permission", devicePermissionPrompter, testContainer)

                describe("when device permission") {
                    forAll(
                        row("granted", true, GetTunerAccess.Result.DeviceAccess.Granted),
                        row("denied", false, GetTunerAccess.Result.DeviceAccess.Denied)
                    ) { resolutionDesc,
                        devicePermissionCallResult,
                        expectedResult ->
                        describe("is $resolutionDesc") {
                            devicePermissionPrompter.prompt(devicePermissionCallResult).thatsIt()

                            it("returns ${expectedResult::class}") {
                                testContainer.getResult() shouldBe expectedResult
                            }
                        }
                    }
                }
            }

            describe("with permanently denied") {
                retrieveClientPermissionPrompter.prompt(ClientPermissionResolution.Permanent.Denied)

                it("returns ${GetTunerAccess.Result.ClientPermissionDeniedPermanently::class}") {
                    testContainer.getResult() shouldBe GetTunerAccess.Result.ClientPermissionDeniedPermanently
                }
            }
        }

        testContainer.close()
    }
})
