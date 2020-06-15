package app.ekaralamov.sdr.driver.permissions

import android.hardware.usb.UsbDevice
import app.ekaralamov.sdr.driver.ClientPermissionResolution
import app.ekaralamov.sdr.driver.ClientPermissionStorage
import javax.inject.Inject
import javax.inject.Provider

class GetTunerAccess @Inject constructor(
    private val clientPermissionStorage: ClientPermissionStorage,
    private val platformDevicePermissionAuthorityProvider: Provider<PlatformDevicePermissionAuthority>
) {

    sealed class Result {

        object ClientPermissionDeniedPermanently : Result()

        sealed class DeviceAccess : Result() {
            object Granted : DeviceAccess()
            object Denied : DeviceAccess()
        }

        class GrantPermissionToClientQuestion internal constructor(
            private val useCase: GetTunerAccess,
            private val clientPackageName: String,
            private val device: UsbDevice,
            permanentDenialOption: Boolean
        ) : Result() {

            val never: (suspend () -> Unit)? =
                if (permanentDenialOption) {
                    {
                        useCase.clientPermissionStorage.storeResolution(
                            clientPackageName,
                            ClientPermissionResolution.Permanent.Denied
                        )
                    }
                } else
                    null

            suspend fun yes(): DeviceAccess = with(useCase) {
                clientPermissionStorage.storeResolution(
                    clientPackageName,
                    ClientPermissionResolution.Permanent.Granted
                )
                getPermissionFor(device)
            }

            suspend fun no() {
                useCase.clientPermissionStorage.storeResolution(
                    clientPackageName,
                    ClientPermissionResolution.Denied
                )
            }
        }
    }

    suspend operator fun invoke(clientPackageName: String, device: UsbDevice): Result {

        fun grantPermissionToClientQuestion(permanentDenialOption: Boolean) =
            Result.GrantPermissionToClientQuestion(
                this@GetTunerAccess,
                clientPackageName,
                device,
                permanentDenialOption
            )

        return when (clientPermissionStorage.retrieveResolutionFor(clientPackageName)) {
            null -> grantPermissionToClientQuestion(permanentDenialOption = false)
            ClientPermissionResolution.Denied -> grantPermissionToClientQuestion(permanentDenialOption = true)
            ClientPermissionResolution.Permanent.Denied -> Result.ClientPermissionDeniedPermanently
            ClientPermissionResolution.Permanent.Granted -> getPermissionFor(device)
        }
    }

    private suspend fun getPermissionFor(device: UsbDevice) =
        if (platformDevicePermissionAuthorityProvider.get().getPermissionFor(device))
            Result.DeviceAccess.Granted
        else
            Result.DeviceAccess.Denied
}
