package app.ekaralamov.sdr.driver

import android.app.Activity
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import androidx.test.platform.app.InstrumentationRegistry

fun findDevice(vendorID: Int, productID: Int): UsbDevice {
    val usbManager = InstrumentationRegistry.getInstrumentation().context
        .getSystemService(Activity.USB_SERVICE) as UsbManager
    val devices = usbManager.deviceList.values
    return devices.single { it.vendorId == vendorID && it.productId == productID }
}
