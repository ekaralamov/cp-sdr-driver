package sdr.driver.cp.opening

import android.hardware.usb.UsbDevice

interface PlatformDeviceLocator {

    fun getDeviceFor(address: DeviceAddress): UsbDevice
}
