package sdr.driver.cp.permissions

import sdr.driver.cp.ClientPermissionRepository
import sdr.driver.cp.ClientPermissionResolution
import sdr.driver.cp.TunerAccessToken
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
