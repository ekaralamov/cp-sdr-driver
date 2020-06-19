package app.ekaralamov.sdr.driver.permissions

import app.ekaralamov.sdr.driver.ClientPermissionRepository
import app.ekaralamov.sdr.driver.ClientPermissionResolution
import javax.inject.Inject

class GrantPackageAccess @Inject constructor(
    private val permissionRepository: ClientPermissionRepository
) {

    suspend operator fun invoke(packageName: String) {
        permissionRepository.storeResolution(packageName, ClientPermissionResolution.Permanent.Granted)
    }
}
