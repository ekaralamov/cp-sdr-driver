package app.ekaralamov.sdr.driver

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TheClientPermissionStorage @Inject constructor() : ClientPermissionStorage {

    override suspend fun retrieveResolutionFor(
        packageName: String
    ): ClientPermissionResolution? = ClientPermissionResolution.Permanent.Granted

    override suspend fun storeResolution(
        packageName: String,
        resolution: ClientPermissionResolution
    ) {
    }

    override fun retrievePermanentResolutions(): Flow<List<Pair<String, ClientPermissionResolution.Permanent>>> =
        emptyFlow()

    override suspend fun deleteResolutionFor(packageName: String) {}
}
