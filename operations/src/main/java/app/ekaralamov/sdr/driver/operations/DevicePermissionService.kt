package app.ekaralamov.sdr.driver.operations

import android.hardware.usb.UsbDevice

interface DevicePermissionService {

    suspend fun getDevicePermission(device: UsbDevice): Boolean
}
