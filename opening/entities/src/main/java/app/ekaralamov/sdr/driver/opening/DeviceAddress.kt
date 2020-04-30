package app.ekaralamov.sdr.driver.opening

import android.net.Uri

internal fun DeviceAddress.Companion.from(uri: Uri): DeviceAddress =
    try {
        with(uri.pathSegments.iterator()) {
            DeviceAddress(
                vendorId = next().toInt(16),
                productId = next().toInt(16),
                path = next()
            )
        }
    } catch (exception: Exception) {
        throw IllegalArgumentException("invalid URI: $uri", exception)
    }
