package sdr.driver.cp

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ClientPermissionRepository @Inject constructor(
    private val storage: ClientPermissionStorage
) {

    suspend fun retrieveResolutionFor(packageName: String) = storage.retrieveResolutionFor(packageName)

    suspend fun storeResolution(
        packageName: String,
        resolution: ClientPermissionResolution
    ) = storage.storeResolution(packageName, resolution)

    suspend fun deleteResolutionFor(packageName: String) = storage.deleteResolutionFor(packageName)

    fun resolutions(): Flow<List<Pair<String, ClientPermissionResolution>>> =
        storage.resolutions()
}
