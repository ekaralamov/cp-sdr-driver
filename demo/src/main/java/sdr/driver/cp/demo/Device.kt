package sdr.driver.cp.demo

import android.app.Activity
import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager

fun device(context: Context): UsbDevice {
    val usbManager = context.getSystemService(Activity.USB_SERVICE) as UsbManager
    val devices = usbManager.deviceList.values
    return devices.single { it.vendorId == 0x0bda && it.productId == 0x2832 }
}
