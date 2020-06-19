package app.ekaralamov.sdr.driver

import kotlinx.coroutines.flow.Flow

interface ClientPermissionStorage {

    suspend fun retrieveResolutionFor(packageName: String): ClientPermissionResolution?

    suspend fun storeResolution(
        packageName: String,
        resolution: ClientPermissionResolution
    )

    fun resolutions(): Flow<List<Pair<String, ClientPermissionResolution>>>

    suspend fun deleteResolutionFor(packageName: String)

//    suspend fun deleteNonPermanentResolutions(olderThan: Duration)
}
