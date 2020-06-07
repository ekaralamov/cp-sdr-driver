package app.ekaralamov.sdr.driver.opening

import android.hardware.usb.UsbDevice
import android.net.Uri
import android.os.ParcelFileDescriptor
import app.ekaralamov.sdr.driver.TunerAccessToken
import javax.inject.Inject

class OpenTuner @Inject constructor(
    private val platformDeviceLocator: PlatformDeviceLocator,
    private val accessTokenRegistry: TunerAccessToken.Registry<UsbDevice, TunerSession>,
    private val nativeSessionFactory: NativeTunerSession.Factory,
    private val sessionFactory: TunerSession.Factory
) {
    private enum class ChannelType { Commands, Data }

    operator fun invoke(uri: Uri, mode: String, callingPackage: String): ParcelFileDescriptor {
        val channelType = when (mode) {
            "w" -> ChannelType.Commands
            "r" -> ChannelType.Data
            else -> throw IllegalArgumentException("invalid mode: $mode")
        }
        val device = platformDeviceLocator.getDeviceFor(DeviceAddress.from(uri))
        with(device) {
            if (!isDeviceSupported(vendorID = vendorId, productID = productId))
                throw UnsupportedOperationException("device $this is not supported")
        }
        return with(
            accessTokenRegistry.acquireToken(
                deviceAddress = device,
                callingPackage = callingPackage,
                sessionFactory = { device ->
                    sessionFactory.create(nativeSessionFactory.create(device))
                }
            )
        ) {
            when (channelType) {
                ChannelType.Commands -> session.startCommandsPump(this)
                ChannelType.Data -> session.startDataPump(this)
            }
        }
    }
}
