package app.ekaralamov.sdr.driver

import kotlinx.coroutines.flow.Flow

interface ClientPermissionStorage {

    suspend fun retrieveResolutionFor(packageName: String): ClientPermissionResolution?

    suspend fun storeResolution(
        packageName: String,
        resolution: ClientPermissionResolution
    )

    fun retrievePermanentResolutions(): Flow<List<Pair<String, ClientPermissionResolution.Permanent>>>

    suspend fun deleteResolutionFor(packageName: String)

//    suspend fun deleteNonPermanentResolutions(olderThan: Duration)
}
