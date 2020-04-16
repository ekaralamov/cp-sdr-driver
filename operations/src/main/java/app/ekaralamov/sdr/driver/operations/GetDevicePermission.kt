package app.ekaralamov.sdr.driver.operations

import android.hardware.usb.UsbDevice

class GetDevicePermission(private val devicePermissionService: DevicePermissionService) {

    suspend operator fun invoke(device: UsbDevice) =
        devicePermissionService.getDevicePermission(device)
}
