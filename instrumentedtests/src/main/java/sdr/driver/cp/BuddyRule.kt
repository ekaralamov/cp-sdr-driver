package sdr.driver.cp

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.hardware.usb.UsbDevice
import android.os.IBinder
import android.os.Looper
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import org.junit.rules.ExternalResource
import sdr.driver.cp.test.buddy.Buddy

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
class BuddyRule private constructor(private val id: String = "one") : ExternalResource(), ServiceConnection {

    private var _buddy: Buddy? = null
    val buddy: Buddy
        get() =
            if (Looper.myLooper() == Looper.getMainLooper())
                throw Exception("buddy must be get() off the main thread")
            else synchronized(this) {
                _buddy ?: run {
                    (this as Object).wait(ConnectTimeout)
                    _buddy ?: throw Exception("no connection to buddy $id service")
                }
            }

    val packageName = "sdr.driver.cp.test.buddy.$id"

    override fun before() {
        val bound = InstrumentationRegistry.getInstrumentation().context.bindService(
            Intent().apply {
                component = ComponentName(
                    packageName,
                    "sdr.driver.cp.test.buddy.MainService"
                )
            },
            this,
            Context.BIND_AUTO_CREATE or Context.BIND_IMPORTANT
        )
        if (!bound)
            throw Exception("binding to buddy $id service failed")
    }

    override fun after() {
        InstrumentationRegistry.getInstrumentation().context.unbindService(this)
    }

    @Synchronized
    override fun onServiceDisconnected(name: ComponentName) {
        _buddy = null
    }

    @Synchronized
    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        _buddy = Buddy.Stub.asInterface(service)
        (this as Object).notifyAll()
    }

    fun getAccess(tuner: UsbDevice) {
        FakeClientPermissionStorage[packageName] = ClientPermissionResolution.Permanent.Granted
        val getAccessRequestKey = buddy.requestAccess(tuner)
        UsbDeviceAccessDialog.answerWithYes()
        assertThat(buddy.waitForResult(getAccessRequestKey)).isEqualTo(Activity.RESULT_OK)
    }

    companion object {
        const val ConnectTimeout = 10_000L

        fun one() = BuddyRule("one")
        fun two() = BuddyRule("two")
    }
}
