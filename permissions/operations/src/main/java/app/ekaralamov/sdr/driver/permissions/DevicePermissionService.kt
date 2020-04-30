package app.ekaralamov.sdr.driver.permissions

import android.hardware.usb.UsbDevice

interface DevicePermissionService {

    suspend fun getDevicePermission(device: UsbDevice): Boolean
}
