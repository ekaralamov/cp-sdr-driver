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

        class AskForClientPermission(
            val permissionGrantedContinuation: suspend () -> DeviceAccess,
            val permissionDeniedContinuation: suspend () -> Unit,
            val permissionDeniedPermanentlyContinuation: (suspend () -> Unit)?
        ) : Result()
    }

    suspend operator fun invoke(clientPackageName: String, device: UsbDevice): Result {

        fun askForClientPermission(permanentDenialOption: Boolean): Result.AskForClientPermission {

            val permissionDeniedPermanentlyContinuation: (suspend () -> Unit)? = if (permanentDenialOption) {
                {
                    clientPermissionStorage.storeResolution(
                        clientPackageName,
                        ClientPermissionResolution.Permanent.Denied
                    )
                }
            } else
                null

            return Result.AskForClientPermission(
                permissionGrantedContinuation = {
                    clientPermissionStorage.storeResolution(
                        clientPackageName,
                        ClientPermissionResolution.Permanent.Granted
                    )
                    getPermissionFor(device)
                },
                permissionDeniedContinuation = {
                    clientPermissionStorage.storeResolution(
                        clientPackageName,
                        ClientPermissionResolution.Denied
                    )
                },
                permissionDeniedPermanentlyContinuation = permissionDeniedPermanentlyContinuation
            )
        }

        return when (clientPermissionStorage.retrieveResolutionFor(clientPackageName)) {
            null -> askForClientPermission(permanentDenialOption = false)
            ClientPermissionResolution.Denied -> askForClientPermission(permanentDenialOption = true)
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
