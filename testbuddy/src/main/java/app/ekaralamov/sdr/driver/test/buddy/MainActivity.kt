package app.ekaralamov.sdr.driver.test.buddy

import android.app.Activity
import android.os.Bundle

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        contentResolver.delete(intent.data ?: throw IllegalArgumentException(), null, null)

    }

    companion object {
        val UsbDeviceExtra = "${this::class.qualifiedName}.UsbDevice"
    }
}
