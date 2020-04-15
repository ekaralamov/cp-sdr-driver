package app.ekaralamov.sdr.driver.test.buddy

import android.content.Context
import android.content.Intent
import app.ekaralamov.sdr.driver.GetTunerAccessDeviceNameExtra

class Endpoints(private val context: Context) : Buddy.Stub() {

    override fun requestAccess(deviceName: String): Int {
        val requestKey = ActivityRegistry.newEntry()
        context.startActivity(Intent(context, MainActivity::class.java).apply {
            putExtra(GetTunerAccessDeviceNameExtra, deviceName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(MainActivity.RequestKeyExtra, requestKey)
        })
        return requestKey
    }

    override fun waitForAccess(requestKey: Int): Int = ActivityRegistry.waitFor(requestKey)
}
