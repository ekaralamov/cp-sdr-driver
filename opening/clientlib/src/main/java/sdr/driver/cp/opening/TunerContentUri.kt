package sdr.driver.cp.opening

import android.content.Context
import android.hardware.usb.UsbDevice
import android.net.Uri
import sdr.driver.cp.opening.client.R

object TunerContentUri {

    fun build(device: UsbDevice, context: Context): Uri = Uri.Builder()
        .scheme("content")
        .authority(context.getString(R.string.cp_sdr_driver_authority))
        .appendPath(device.deviceName)
        .build()
}
