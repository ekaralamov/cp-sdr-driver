package app.ekaralamov.sdr.driver.opening

import android.hardware.usb.UsbDevice

interface PlatformDeviceLocator {

    fun getDeviceFor(address: DeviceAddress): UsbDevice
}
