package sdr.driver.cp.opening

import android.hardware.usb.UsbManager
import java.io.FileNotFoundException
import javax.inject.Inject

class ThePlatformDeviceLocator @Inject constructor(private val usbManager: UsbManager) :
    PlatformDeviceLocator {

    override fun getDeviceFor(address: DeviceAddress) = usbManager.deviceList[address.path]
        ?: throw FileNotFoundException("no USB device at the specified path: ${address.path}")
}
