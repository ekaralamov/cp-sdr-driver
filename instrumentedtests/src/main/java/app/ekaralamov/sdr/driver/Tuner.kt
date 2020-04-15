package app.ekaralamov.sdr.driver

import android.app.Activity
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry

object Tuner {
    fun contentUri(vendorID: Int, productID: Int): Uri {
        val deviceName = findDevice(vendorID = vendorID, productID = productID).deviceName
        return Uri.Builder()
            .scheme("content")
            .authority(
                InstrumentationRegistry.getInstrumentation().targetContext
                    .getString(R.string.prinos_sdr_driver_authority)
            )
            .appendPath(vendorID.toString(16))
            .appendPath(productID.toString(16))
            .appendPath(deviceName)
            .build()
    }

    fun findDevice(vendorID: Int, productID: Int): UsbDevice {
        val usbManager = InstrumentationRegistry.getInstrumentation().context
            .getSystemService(Activity.USB_SERVICE) as UsbManager
        val devices = usbManager.deviceList.values
        return devices.single { it.vendorId == vendorID && it.productId == productID }
    }
}
