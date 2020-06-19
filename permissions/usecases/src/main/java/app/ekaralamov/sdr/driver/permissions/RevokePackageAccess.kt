package app.ekaralamov.sdr.driver.permissions

import app.ekaralamov.sdr.driver.ClientPermissionRepository
import app.ekaralamov.sdr.driver.ClientPermissionResolution
import app.ekaralamov.sdr.driver.TunerAccessToken
import javax.inject.Inject

class RevokePackageAccess @Inject constructor(
    private val permissionRepository: ClientPermissionRepository,
    private val accessTokenRegistry: TunerAccessToken.Registry<*, *>
) {

    suspend operator fun invoke(packageName: String) {
        permissionRepository.storeResolution(packageName, ClientPermissionResolution.Permanent.Denied)
        accessTokenRegistry.revokeTokensFor(packageName)
    }
}
