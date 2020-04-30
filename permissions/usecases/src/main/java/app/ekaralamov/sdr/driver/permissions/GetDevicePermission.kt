package app.ekaralamov.sdr.driver.permissions

import android.hardware.usb.UsbDevice
import javax.inject.Inject

class GetDevicePermission @Inject constructor(private val devicePermissionService: DevicePermissionService) {

    suspend operator fun invoke(device: UsbDevice) =
        devicePermissionService.getDevicePermission(device)
}
