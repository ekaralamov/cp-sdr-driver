package app.ekaralamov.sdr.driver.permissions

import app.ekaralamov.sdr.driver.ClientPermissionRepository
import app.ekaralamov.sdr.driver.ClientPermissionResolution
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.coroutineContext

class ListAccessResolutions @Inject constructor(
    private val permissionRepository: ClientPermissionRepository,
    @Named("default") private val mapDispatcher: CoroutineDispatcher
) {

    operator fun invoke(): Flow<List<Pair<String, Boolean>>> =
        permissionRepository.resolutions().mapLatest { repoList ->
            repoList.map {
                coroutineContext.ensureActive()
                it.first to (it.second == ClientPermissionResolution.Permanent.Granted)
            }
        }.flowOn(mapDispatcher)
}
