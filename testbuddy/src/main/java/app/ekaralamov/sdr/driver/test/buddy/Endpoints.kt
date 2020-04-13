package app.ekaralamov.sdr.driver.test.buddy

import android.content.Context
import android.content.Intent
import android.net.Uri

class Endpoints(private val context: Context) : Buddy.Stub() {

    override fun requestAccess(uri: Uri): Int {
        val requestKey = ActivityRegistry.newEntry()
        context.startActivity(Intent(context, MainActivity::class.java).apply {
            data = uri
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(MainActivity.RequestKeyExtra, requestKey)
        })
        return requestKey
    }

    override fun waitForAccess(requestKey: Int): Boolean = ActivityRegistry.waitFor(requestKey)
}
