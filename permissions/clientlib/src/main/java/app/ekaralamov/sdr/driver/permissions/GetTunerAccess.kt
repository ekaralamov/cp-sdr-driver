package app.ekaralamov.sdr.driver.permissions

import android.content.ComponentName
import android.content.Intent
import android.hardware.usb.UsbDevice

fun getTunerAccessIntent(device: UsbDevice) = Intent().apply {
    component = ComponentName(
        "app.ekaralamov.sdr.driver",
        "app.ekaralamov.sdr.driver.permissions.GetTunerAccessActivity"
    )
    putExtra(GetTunerAccessConstants.DeviceExtra, device)
}
