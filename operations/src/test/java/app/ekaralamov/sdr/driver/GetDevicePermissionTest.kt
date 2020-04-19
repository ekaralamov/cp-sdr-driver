package app.ekaralamov.sdr.driver

import android.hardware.usb.UsbDevice
import app.ekaralamov.test.answerPrompterFor
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlin.coroutines.coroutineContext

class GetDevicePermissionTest : DescribeSpec({
    describe("GetDevicePermission invocation for a given device") {
        val device = mockk<UsbDevice>()

        val devicePermissionService = mockk<DevicePermissionService>()
        val devicePermissionPrompter =
            answerPrompterFor(coEvery { devicePermissionService.getDevicePermission(device) })

        val sut = GetDevicePermission(devicePermissionService)

        val supervisorJob = SupervisorJob(checkNotNull(coroutineContext[Job]))
        val deferredResult =
            GlobalScope.async(TestCoroutineDispatcher() + supervisorJob) {
                sut(device)
            }

        forAll(
            row(false),
            row(true)
        ) { getPermissionResult ->
            context("devicePermissionService.getDevicePermission() answers '$getPermissionResult' for that device") {
                devicePermissionPrompter.prompt(getPermissionResult)

                it("returns $getPermissionResult") {
                    deferredResult.getCompleted() shouldBe getPermissionResult
                }
            }
        }

        context("devicePermissionService.getDevicePermission() throws exception for that device") {
            devicePermissionPrompter.prompt(Exception("test exception"))

            it("throws exception") {
                shouldThrowAny {
                    deferredResult.getCompleted()
                }
            }
        }

        context("is cancelled") {
            supervisorJob.cancel()

            it("cancels devicePermissionService.getDevicePermission() invocation") {
                devicePermissionPrompter.isCancelled shouldBe true
            }
        }

        supervisorJob.complete()
    }
})
