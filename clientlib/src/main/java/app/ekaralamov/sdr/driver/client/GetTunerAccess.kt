package app.ekaralamov.sdr.driver.client

import android.content.ComponentName
import android.content.Intent
import android.hardware.usb.UsbDevice
import app.ekaralamov.sdr.driver.GetTunerAccessDeviceExtra

fun getTunerAccessIntent(device: UsbDevice) = Intent().apply {
    component = ComponentName(
        "app.ekaralamov.sdr.driver",
        "app.ekaralamov.sdr.driver.GetTunerAccessActivity"
    )
    putExtra(GetTunerAccessDeviceExtra, device)
}
