package sdr.driver.cp.permissions

import sdr.driver.cp.ClientPermissionRepository
import sdr.driver.cp.ClientPermissionResolution
import javax.inject.Inject

class GrantPackageAccess @Inject constructor(
    private val permissionRepository: ClientPermissionRepository
) {

    suspend operator fun invoke(packageName: String) {
        permissionRepository.storeResolution(packageName, ClientPermissionResolution.Permanent.Granted)
    }
}
