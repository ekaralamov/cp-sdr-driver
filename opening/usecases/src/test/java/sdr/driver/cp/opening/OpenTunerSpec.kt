package sdr.driver.cp.opening

import android.hardware.usb.UsbDevice
import android.net.Uri
import android.os.ParcelFileDescriptor
import sdr.driver.cp.DeviceBusyException
import sdr.driver.cp.TunerAccessToken
import sdr.driver.cp.test.one
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import io.mockk.*
import java.io.FileNotFoundException

class OpenTunerSpec : DescribeSpec({
    describe("when OpenTuner is invoked") {
        val platformDeviceLocator = mockk<PlatformDeviceLocator>()
        val accessTokenRegistry = mockk<TunerAccessToken.Registry<UsbDevice, TunerSession>>()
        val nativeSessionFactory = mockk<NativeTunerSession.Factory>()
        val sessionFactory = mockk<TunerSession.Factory>()

        val sut = OpenTuner(platformDeviceLocator, accessTokenRegistry, nativeSessionFactory, sessionFactory)

        val uri = mockk<Uri>()
        mockkObject(DeviceAddress.Companion)

        describe("with invalid mode") {
            val mode = "rw"
            it("throws IllegalArgumentException") {
                shouldThrow<IllegalArgumentException> {
                    sut(uri, mode, "package")
                }
            }
        }

        forAll(
            row("w", TunerSession::startCommandsPump, "commands"),
            row("r", TunerSession::startDataPump, "data")
        ) { mode, startPump: TunerSession.(TunerAccessToken<UsbDevice, TunerSession>) -> ParcelFileDescriptor, pumpName ->
            describe("with \"$mode\" mode") {
                context("conversion of the given URI to device address throws IllegalArgumentException") {
                    every { DeviceAddress.from(uri) } throws IllegalArgumentException("test exception")

                    it("throws IllegalArgumentException") {
                        shouldThrow<IllegalArgumentException> {
                            sut(uri, mode, "package")
                        }
                    }
                }

                context("conversion of the given URI to device address throws an exception") {
                    every { DeviceAddress.from(uri) } throws Exception("test exception")

                    it("throws an exception") {
                        shouldThrowAny {
                            sut(uri, mode, "package")
                        }
                    }
                }

                context("conversion of the given URI to device address succeeds") {
                    every { DeviceAddress.from(uri) } returns DeviceAddress("device path")

                    context("locating device throws FileNotFoundException") {
                        every {
                            platformDeviceLocator.getDeviceFor(DeviceAddress("device path"))
                        } throws FileNotFoundException("test exception")

                        it("throws FileNotFoundException") {
                            shouldThrow<FileNotFoundException> {
                                sut(uri, mode, "package")
                            }
                        }
                    }

                    context("locating device throws an exception") {
                        every {
                            platformDeviceLocator.getDeviceFor(DeviceAddress("device path"))
                        } throws Exception("test exception")

                        it("throws an exception") {
                            shouldThrowAny {
                                sut(uri, mode, "package")
                            }
                        }
                    }

                    context("locating device succeeds") {
                        val device = mockk<UsbDevice> {
                            every { vendorId } returns 1
                            every { productId } returns 2
                        }
                        every {
                            platformDeviceLocator.getDeviceFor(DeviceAddress("device path"))
                        } returns device

                        mockkStatic("sdr.driver.cp.opening.NativeCallsKt")

                        context("device is not supported") {
                            every { isDeviceSupported(vendorID = 1, productID = 2) } returns false

                            it("throws UnsupportedOperationException") {
                                shouldThrow<UnsupportedOperationException> {
                                    sut(uri, mode, "package")
                                }
                            }
                        }

                        context("device is supported") {
                            every { isDeviceSupported(vendorID = 1, productID = 2) } returns true

                            describe("when token acquisition fails with DeviceBusyException") {
                                every { accessTokenRegistry.acquireToken(device, "package", any()) } throws DeviceBusyException()

                                it("throws DeviceBusyException") {
                                    shouldThrow<DeviceBusyException> {
                                        sut(uri, mode, "package")
                                    }
                                }
                            }

                            describe("when token acquisition fails with SecurityException") {
                                every { accessTokenRegistry.acquireToken(device, "package", any()) } throws SecurityException()

                                it("throws SecurityException") {
                                    shouldThrow<SecurityException> {
                                        sut(uri, mode, "package")
                                    }
                                }
                            }

                            describe("when token acquisition succeeds") {
                                val session = mockk<TunerSession>()
                                val token = mockk<TunerAccessToken<UsbDevice, TunerSession>> {
                                    every { this@mockk.session } returns session
                                }
                                val fd = mockk<ParcelFileDescriptor>()
                                one { session.startPump(token) } returns fd
                                val sessionFactorySlot = slot<(UsbDevice) -> TunerSession>()
                                one { accessTokenRegistry.acquireToken(device, "package", capture(sessionFactorySlot)) } returns token

                                val result = sut(uri, mode, "package")

                                it("had provided a compound session factory") {
                                    val nativeSession = mockk<NativeTunerSession>()
                                    one { nativeSessionFactory.create(device) } returns nativeSession
                                    one { sessionFactory.create(nativeSession) } returns session

                                    sessionFactorySlot.invoke(device) shouldBe session
                                }

                                it("returns the session's $pumpName pump client pipe end") {
                                    result shouldBe fd
                                }
                            }
                        }
                    }
                }
            }
        }
    }
})
