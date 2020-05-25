package app.ekaralamov.sdr.driver.opening

import android.content.Context
import android.hardware.usb.UsbDevice
import android.net.Uri
import app.ekaralamov.sdr.driver.opening.client.R

object TunerContentUri {

    fun build(device: UsbDevice, context: Context): Uri = Uri.Builder()
        .scheme("content")
        .authority(context.getString(R.string.prinos_sdr_driver_authority))
        .appendPath(device.deviceName)
        .build()
}