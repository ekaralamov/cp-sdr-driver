package app.ekaralamov.sdr.driver

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.rules.ExternalResource

class BuddyRule : ExternalResource(), ServiceConnection {

    private var bound = false

    override fun before() {
        bound = InstrumentationRegistry.getInstrumentation().context.bindService(
            Intent().apply {
                component = ComponentName(
                    "app.ekaralamov.sdr.driver.test.buddy",
                    "app.ekaralamov.sdr.driver.test.buddy.MainService"
                )
            },
            this,
            Context.BIND_AUTO_CREATE
        )
    }

    override fun after() {
        if (bound)
            InstrumentationRegistry.getInstrumentation().context.unbindService(this)
    }

    override fun onServiceDisconnected(p0: ComponentName?) {

    }

    override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
        Log.d("BuddyRule", "connected")
    }
}
