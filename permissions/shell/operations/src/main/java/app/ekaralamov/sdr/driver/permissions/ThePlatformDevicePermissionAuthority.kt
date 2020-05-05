package app.ekaralamov.sdr.driver.permissions

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import kotlinx.coroutines.channels.Channel
import javax.inject.Inject

class ThePlatformDevicePermissionAuthority @Inject constructor(
    private val context: Context,
    private val usbManager: UsbManager
) : PlatformDevicePermissionAuthority {

    override suspend fun getPermissionFor(device: UsbDevice): Boolean {
        val channel = Channel<Boolean>(Channel.CONFLATED)
        val broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) = with(intent) {
                if (action == ActionUsbPermission &&
                    device == getParcelableExtra(UsbManager.EXTRA_DEVICE)
                ) channel.offer(getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false))
            }
        }
        context.registerReceiver(broadcastReceiver, IntentFilter(ActionUsbPermission))
        try {
            usbManager.requestPermission(
                device,
                PendingIntent.getBroadcast(context, 0, Intent(ActionUsbPermission), 0)
            )
            return channel.receive()
        } finally {
            context.unregisterReceiver(broadcastReceiver)
        }
    }

    companion object {

        private const val ActionUsbPermission = "app.ekaralamov.sdr.driver.usbpermission"
    }
}
