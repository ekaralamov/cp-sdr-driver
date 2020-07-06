package sdr.driver.cp.permissions

import android.content.ComponentName
import android.content.Intent
import android.hardware.usb.UsbDevice

fun TunerAccessClient.intent(device: UsbDevice) = Intent().apply {
    component = ComponentName(
        "sdr.driver.cp",
        "sdr.driver.cp.permissions.GetTunerAccessActivity"
    )
    putExtra(TunerAccessClient.Extra.Device, device)
}
