package app.ekaralamov.sdr.driver.opening

import android.net.Uri
import android.os.ParcelFileDescriptor
import javax.inject.Inject

class OpenTuner @Inject constructor(private val deviceLocator: DeviceLocator) {

    operator fun invoke(uri: Uri): ParcelFileDescriptor {
//        val address = DeviceAddress.from(uri)
        throw NotImplementedError()
    }
}
