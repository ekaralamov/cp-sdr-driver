package sdr.driver.cp.permissions

import android.hardware.usb.UsbDevice

interface PlatformDevicePermissionAuthority {

    suspend fun getPermissionFor(device: UsbDevice): Boolean
}
