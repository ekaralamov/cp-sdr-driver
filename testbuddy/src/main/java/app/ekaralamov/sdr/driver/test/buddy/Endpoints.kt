package app.ekaralamov.sdr.driver.test.buddy

import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import app.ekaralamov.sdr.driver.GetTunerAccessDeviceExtra

class Endpoints(private val context: Context) : Buddy.Stub() {

    override fun requestAccess(device: UsbDevice): Int {
        val requestKey = ActivityRegistry.newEntry()
        context.startActivity(Intent(context, MainActivity::class.java).apply {
            putExtra(GetTunerAccessDeviceExtra, device)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(MainActivity.RequestKeyExtra, requestKey)
        })
        return requestKey
    }

    override fun waitForAccess(requestKey: Int): Int = ActivityRegistry.waitFor(requestKey)
}
