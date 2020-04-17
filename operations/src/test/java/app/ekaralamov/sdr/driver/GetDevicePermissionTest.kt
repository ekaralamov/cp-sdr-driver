package app.ekaralamov.sdr.driver

import android.hardware.usb.UsbDevice
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe

class GetDevicePermissionTest : DescribeSpec({
    describe("GetDevicePermission invocation for a given device") {
        val device = mock<UsbDevice>()

        val devicePermissionService = mock<DevicePermissionService>()

        val sut = GetDevicePermission(devicePermissionService)

        forAll(
            row(false),
            row(true)
        ) { getPermissionResult ->
            context("devicePermissionService.getDevicePermission() returns '$getPermissionResult' for that device") {
                whenever(devicePermissionService.getDevicePermission(device))
                    .thenReturn(getPermissionResult)

                val result = sut(device)

                it("returns $getPermissionResult") {
                    result shouldBe getPermissionResult
                }

                it("calls devicePermissionService.getDevicePermission() once") {
                    verify(devicePermissionService).getDevicePermission(device)
                }
            }
        }
    }
})
