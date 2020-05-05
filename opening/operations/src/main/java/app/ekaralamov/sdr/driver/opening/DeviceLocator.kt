package app.ekaralamov.sdr.driver.opening

import android.hardware.usb.UsbDevice
import java.io.FileNotFoundException
import javax.inject.Inject

class DeviceLocator @Inject constructor(private val platformDeviceLocator: PlatformDeviceLocator) {

    fun getDeviceFor(address: DeviceAddress): UsbDevice =
        platformDeviceLocator.getDeviceFor(address.path).apply {
            if (vendorId != address.vendorId || productId != address.productId)
                throw FileNotFoundException("$this does not match $address")
        }
}
