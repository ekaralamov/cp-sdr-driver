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
import io.mockk.coEvery
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

        val retrieveClientPermissionPrompter = answerPrompterFor(
            coEvery { clientPermissionStorage.retrieveResolutionFor("client package name") }
        )

        val testContainer = CoroutineTestContainer.run {
            sut("client package name", device)
        }

        it("waits for client permission resolution retrieval") {
            retrieveClientPermissionPrompter.lastCallState shouldBe AnswerPrompter.CallState.Waiting
        }

        describe("when cancelled while waiting for client permission resolution retrieval") {
            testContainer.cancel()

            it("cancels the permission resolution call") {
                retrieveClientPermissionPrompter.lastCallState shouldBe AnswerPrompter.CallState.Cancelled
            }
        }

        describe("when permission storage responds") {

            forAll(
                row("no resolution", null as ClientPermissionResolution?, false),
                row("denied", ClientPermissionResolution.Denied, true)
            ) { responseDescription,
                clientPermissionResponse,
                permanentDenialOptionPresent ->

                describe("with $responseDescription") {
                    retrieveClientPermissionPrompter.prompt(clientPermissionResponse).thatsIt()

                    val askForClientPermission: GetTunerAccess.Result.AskForClientPermission by nosynchLazy {
                        testContainer.getResult() as GetTunerAccess.Result.AskForClientPermission
                    }

                    if (permanentDenialOptionPresent) {
                        val permissionDeniedPermanentlyContinuation: (suspend () -> Unit) by nosynchLazy {
                            askForClientPermission.permissionDeniedPermanentlyContinuation!!
                        }

                        it("returns AskForClientPermission with permanent denial option") {
                            shouldNotThrowAny { permissionDeniedPermanentlyContinuation }
                        }

                        describe("when permission denied permanently continuation is invoked") {
                            val storeClientPermissionPrompter = answerPrompterFor(coEvery {
                                clientPermissionStorage.storeResolution(
                                    "client package name",
                                    ClientPermissionResolution.Permanent.Denied
                                )
                            })

                            val testContainer = CoroutineTestContainer.run {
                                permissionDeniedPermanentlyContinuation()
                            }

                            it("calls for storing client permission permanently denied resolution and waits for it to complete") {
                                storeClientPermissionPrompter.lastCallState shouldBe AnswerPrompter.CallState.Waiting
                            }

                            describe("when cancelled while waiting for client permission resolution storing") {
                                testContainer.cancel()

                                it("cancels the permission resolution call") {
                                    storeClientPermissionPrompter.lastCallState shouldBe AnswerPrompter.CallState.Cancelled
                                }
                            }

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
                        it("returns AskForClientPermission without permanent denial option") {
                            askForClientPermission.permissionDeniedPermanentlyContinuation shouldBe null
                        }

                    describe("when permission denied _not permanently_ continuation is invoked") {
                        val storeClientPermissionPrompter = answerPrompterFor(coEvery {
                            clientPermissionStorage.storeResolution(
                                "client package name",
                                ClientPermissionResolution.Denied
                            )
                        })

                        val testContainer = CoroutineTestContainer.run {
                            askForClientPermission.permissionDeniedContinuation()
                        }

                        it("calls for storing client permission denied resolution and waits for it to complete") {
                            storeClientPermissionPrompter.lastCallState shouldBe AnswerPrompter.CallState.Waiting
                        }

                        describe("when cancelled while waiting for client permission resolution storing") {
                            testContainer.cancel()

                            it("cancels the permission resolution call") {
                                storeClientPermissionPrompter.lastCallState shouldBe AnswerPrompter.CallState.Cancelled
                            }
                        }

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

                    describe("when permission granted continuation is invoked") {
                        val storeClientPermissionPrompter = answerPrompterFor(coEvery {
                            clientPermissionStorage.storeResolution(
                                "client package name",
                                ClientPermissionResolution.Permanent.Granted
                            )
                        })

                        val testContainer = CoroutineTestContainer.run {
                            askForClientPermission.permissionGrantedContinuation()
                        }

                        it("calls for storing client permission granted resolution and waits for it to complete") {
                            storeClientPermissionPrompter.lastCallState shouldBe AnswerPrompter.CallState.Waiting
                        }

                        describe("when cancelled while waiting for client permission resolution storing") {
                            testContainer.cancel()

                            it("cancels the permission resolution call") {
                                storeClientPermissionPrompter.lastCallState shouldBe AnswerPrompter.CallState.Cancelled
                            }
                        }

                        describe("when the client permission resolution is stored") {
                            val platformDevicePermissionAuthority =
                                mockk<PlatformDevicePermissionAuthority>()
                            one { platformDevicePermissionAuthorityProvider.get() } returns platformDevicePermissionAuthority
                            val devicePermissionPrompter = answerPrompterFor(coEvery {
                                platformDevicePermissionAuthority.getPermissionFor(device)
                            })

                            storeClientPermissionPrompter.prompt(Unit).thatsIt()

                            it("waits for device permission") {
                                devicePermissionPrompter.lastCallState shouldBe AnswerPrompter.CallState.Waiting
                            }

                            describe("when cancelled while waiting for device permission") {
                                testContainer.cancel()

                                it("cancels the device permission call") {
                                    devicePermissionPrompter.lastCallState shouldBe AnswerPrompter.CallState.Cancelled
                                }
                            }

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
                val devicePermissionPrompter = answerPrompterFor(coEvery {
                    platformDevicePermissionAuthority.getPermissionFor(device)
                })

                retrieveClientPermissionPrompter.prompt(ClientPermissionResolution.Permanent.Granted)
                    .thatsIt()

                it("waits for device permission") {
                    devicePermissionPrompter.lastCallState shouldBe AnswerPrompter.CallState.Waiting
                }

                describe("when cancelled while waiting for device permission") {
                    testContainer.cancel()

                    it("cancels the device permission call") {
                        devicePermissionPrompter.lastCallState shouldBe AnswerPrompter.CallState.Cancelled
                    }
                }

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
