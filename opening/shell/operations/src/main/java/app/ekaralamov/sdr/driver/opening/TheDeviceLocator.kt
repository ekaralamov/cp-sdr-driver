package app.ekaralamov.sdr.driver.opening

import android.hardware.usb.UsbManager
import java.io.FileNotFoundException
import javax.inject.Inject

class TheDeviceLocator @Inject constructor(private val usbManager: UsbManager) :
    DeviceLocator {

    override fun get(path: String) = usbManager.deviceList[path]
        ?: throw FileNotFoundException("no USB device at the specified path: $path")
}
