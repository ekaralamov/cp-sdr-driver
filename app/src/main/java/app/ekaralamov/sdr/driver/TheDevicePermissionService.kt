package app.ekaralamov.sdr.driver

import android.hardware.usb.UsbDevice
import javax.inject.Inject

class TheDevicePermissionService @Inject constructor() : DevicePermissionService {

    override suspend fun getDevicePermission(device: UsbDevice): Boolean {
        return true
    }
}
