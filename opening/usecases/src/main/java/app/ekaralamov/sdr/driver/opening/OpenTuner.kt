package app.ekaralamov.sdr.driver.opening

import android.hardware.usb.UsbDevice
import android.net.Uri
import android.os.ParcelFileDescriptor
import app.ekaralamov.sdr.driver.TunerAccessToken
import javax.inject.Inject

class OpenTuner @Inject constructor(
    private val platformDeviceLocator: PlatformDeviceLocator,
    private val accessTokenRegistry: TunerAccessToken.Registry<UsbDevice, TunerSession>,
    private val sessionFactory: TunerSession.Factory
) {

    operator fun invoke(uri: Uri, mode: String, callingPackage: String): ParcelFileDescriptor {
        val device = platformDeviceLocator.getDeviceFor(DeviceAddress.from(uri))
        with(device) {
            if (!NativeCalls.isDeviceSupported(vendorID = vendorId, productID = productId))
                throw UnsupportedOperationException("device $this is not supported")
        }
        return with(
            accessTokenRegistry.acquireToken(
                deviceAddress = device,
                callingPackage = callingPackage,
                sessionFactory = { device ->
                    sessionFactory.create(device)
                }
            )
        ) {
            when (mode) {
                "w" -> session.startCommandsPump(this)
                "r" -> session.startDataPump(this)
                else -> throw IllegalArgumentException("invalid mode: $mode")
            }
        }
    }
}
