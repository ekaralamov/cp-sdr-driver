package app.ekaralamov.sdr.driver.test.buddy

import android.app.Service
import android.content.Intent
import android.hardware.usb.UsbDevice

class MainService : Service() {

    private val binder = object : Buddy.Stub() {
        override fun requestAccess(device: UsbDevice?) {
            TODO("Not yet implemented")
        }
    }

    override fun onBind(intent: Intent?) = binder
}
