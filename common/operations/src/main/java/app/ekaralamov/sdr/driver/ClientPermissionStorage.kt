package app.ekaralamov.sdr.driver

import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

interface ClientPermissionStorage {

    suspend fun retrieveResolutionFor(packageName: String): ClientPermissionResolution?

    suspend fun storeResolution(
        packageName: String,
        resolution: ClientPermissionResolution
    )

    suspend fun retrievePermanentResolutions(): Flow<List<Pair<String, ClientPermissionResolution.Permanent>>>

    suspend fun deleteResolutionFor(packageName: String)

    suspend fun deleteNonPermanentResolutions(olderThan: Duration)
}
