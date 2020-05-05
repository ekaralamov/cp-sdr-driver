package app.ekaralamov.sdr.driver.permissions

import android.hardware.usb.UsbDevice

interface PlatformDevicePermissionAuthority {

    suspend fun getPermissionFor(device: UsbDevice): Boolean
}
