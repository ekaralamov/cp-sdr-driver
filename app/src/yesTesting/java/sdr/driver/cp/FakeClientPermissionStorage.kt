package sdr.driver.cp

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.ConcurrentHashMap

object FakeClientPermissionStorage : ClientPermissionStorage {

    private val permissions = ConcurrentHashMap<String, ClientPermissionResolution>()
    private val resolutionsList = MutableStateFlow<List<Pair<String, ClientPermissionResolution>>>(emptyList())

    override suspend fun retrieveResolutionFor(packageName: String): ClientPermissionResolution? =
        get(packageName)

    override suspend fun storeResolution(packageName: String, resolution: ClientPermissionResolution) =
        set(packageName, resolution)

    override fun resolutions(): Flow<List<Pair<String, ClientPermissionResolution>>> = resolutionsList

    override suspend fun deleteResolutionFor(packageName: String) {
        TODO("Not yet implemented")
    }

    fun clear() {
        permissions.clear()
        resolutionsList.value = emptyList()
    }

    operator fun set(packageName: String, resolution: ClientPermissionResolution) {
        permissions[packageName] = resolution
        resolutionsList.value = permissions.toList()
    }

    operator fun get(packageName: String): ClientPermissionResolution? =
        permissions[packageName]
}
