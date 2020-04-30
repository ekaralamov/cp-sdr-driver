package app.ekaralamov.sdr.driver.opening

import android.hardware.usb.UsbDevice
import android.net.Uri
import java.io.FileNotFoundException

interface DeviceLocator {

    operator fun get(path: String): UsbDevice
}

internal operator fun DeviceLocator.get(address: DeviceAddress): UsbDevice =
    get(address.path).apply {
        if (vendorId != address.vendorId || productId != address.productId)
            throw FileNotFoundException("$this does not match $address")
    }
