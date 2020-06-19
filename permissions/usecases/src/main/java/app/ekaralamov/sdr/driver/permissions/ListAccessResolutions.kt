package app.ekaralamov.sdr.driver.permissions

import app.ekaralamov.sdr.driver.ClientPermissionRepository
import app.ekaralamov.sdr.driver.ClientPermissionResolution
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Named

class ListAccessResolutions @Inject constructor(
    private val permissionRepository: ClientPermissionRepository,
    @Named("Default") private val mapDispatcher: CoroutineDispatcher
) {

    operator fun invoke(): Flow<List<Pair<String, Boolean>>> =
        permissionRepository.resolutions().map { repoList ->
            repoList.map {
                it.first to (it.second == ClientPermissionResolution.Permanent.Granted)
            }
        }.flowOn(mapDispatcher)
}
