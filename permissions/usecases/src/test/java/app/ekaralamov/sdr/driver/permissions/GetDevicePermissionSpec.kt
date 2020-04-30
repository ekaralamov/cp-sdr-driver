package app.ekaralamov.sdr.driver.permissions

import android.hardware.usb.UsbDevice
import app.ekaralamov.test.CoroutineTestContainer
import app.ekaralamov.test.answerPrompterFor
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk

class GetDevicePermissionSpec : DescribeSpec({
    describe("GetDevicePermission invocation for a given device") {
        val device = mockk<UsbDevice>()

        val devicePermissionService = mockk<DevicePermissionService>()
        val devicePermissionPrompter =
            answerPrompterFor(coEvery { devicePermissionService.getDevicePermission(device) })

        val sut = GetDevicePermission(devicePermissionService)

        val testContainer = CoroutineTestContainer.run {
            sut(device)
        }

        forAll(
            row(false),
            row(true)
        ) { getPermissionResult ->
            context("devicePermissionService.getDevicePermission() answers '$getPermissionResult' for that device") {
                devicePermissionPrompter.prompt(getPermissionResult)

                it("returns $getPermissionResult") {
                    testContainer.getResult() shouldBe getPermissionResult
                }
            }
        }

        context("devicePermissionService.getDevicePermission() throws exception for that device") {
            devicePermissionPrompter.prompt(Exception("test exception"))

            it("throws exception") {
                shouldThrowAny {
                    testContainer.getResult()
                }
            }
        }

        context("is cancelled") {
            testContainer.cancel()

            it("cancels devicePermissionService.getDevicePermission() invocation") {
                devicePermissionPrompter.isCancelled shouldBe true
            }
        }

        testContainer.close()
    }
})
