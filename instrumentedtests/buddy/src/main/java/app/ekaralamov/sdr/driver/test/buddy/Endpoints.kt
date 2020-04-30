package app.ekaralamov.sdr.driver.test.buddy

import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import app.ekaralamov.sdr.driver.permissions.GetTunerAccessConstants

class Endpoints(private val context: Context) : Buddy.Stub() {

    override fun requestAccess(device: UsbDevice): Int {
        val requestKey = ActivityRegistry.newEntry()
        context.startActivity(Intent(context, AccessWantingActivity::class.java).apply {
            putExtra(GetTunerAccessConstants.DeviceExtra, device)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(AccessWantingActivity.RequestKeyExtra, requestKey)
        })
        return requestKey
    }

    override fun waitForResult(requestKey: Int): Int = ActivityRegistry.waitFor(requestKey)
}
