package app.ekaralamov.sdr.driver

import android.net.Uri

fun buildContentUri(tunerId: String): Uri = Uri.Builder()
    .scheme("content")
    .authority(DriverApplication.instance.getString(R.string.provider_authority))
    .path(tunerId)
    .build()
