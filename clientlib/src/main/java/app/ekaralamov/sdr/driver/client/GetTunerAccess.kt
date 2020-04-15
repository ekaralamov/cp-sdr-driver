package app.ekaralamov.sdr.driver.client

import android.content.ComponentName
import android.content.Intent
import app.ekaralamov.sdr.driver.GetTunerAccessDeviceNameExtra

fun getTunerAccessIntent(deviceName: String) = Intent().apply {
    component = ComponentName(
        "app.ekaralamov.sdr.driver",
        "app.ekaralamov.sdr.driver.GetTunerAccessActivity"
    )
    putExtra(GetTunerAccessDeviceNameExtra, deviceName)
}
