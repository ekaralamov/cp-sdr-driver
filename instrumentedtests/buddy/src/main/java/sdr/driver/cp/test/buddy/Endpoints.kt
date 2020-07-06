package sdr.driver.cp.test.buddy

import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import sdr.driver.cp.opening.TunerContentUri
import sdr.driver.cp.permissions.TunerAccessClient

class Endpoints(private val context: Context) : Buddy.Stub() {

    override fun requestAccess(device: UsbDevice): Int {
        val requestKey = ActivityRegistry.newEntry()
        context.startActivity(Intent(context, AccessWantingActivity::class.java).apply {
            putExtra(TunerAccessClient.Extra.Device, device)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(AccessWantingActivity.RequestKeyExtra, requestKey)
        })
        return requestKey
    }

    override fun waitForResult(requestKey: Int): Int = ActivityRegistry.waitFor(requestKey)

    override fun openCommandsChannel(device: UsbDevice) =
        context.contentResolver.openFileDescriptor(TunerContentUri.build(device, context), "w")

    override fun openDataChannel(device: UsbDevice) =
        context.contentResolver.openFileDescriptor(TunerContentUri.build(device, context), "r")
}
