package app.ekaralamov.sdr.driver.opening

import android.net.Uri

inline class DeviceAddress(val path: String) {

    companion object {

        fun from(uri: Uri): DeviceAddress = try {
            DeviceAddress(uri.pathSegments[0])
        } catch (exception: Exception) {
            throw IllegalArgumentException("invalid URI: $uri", exception)
        }
    }
}
