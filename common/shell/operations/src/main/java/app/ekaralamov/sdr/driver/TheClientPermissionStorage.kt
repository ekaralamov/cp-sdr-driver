package app.ekaralamov.sdr.driver

import app.ekaralamov.sdr.driver.operations.shell.Database
import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class TheClientPermissionStorage @Inject constructor(
    @Named("permissions") sqlDriver: SqlDriver,
    @Named("IO") private val ioDispatcher: CoroutineDispatcher
) : ClientPermissionStorage {

    private val queries = Database(
        sqlDriver,
        PermissionResolutions.Adapter(
            object : ColumnAdapter<ClientPermissionResolution, Long> {
                override fun decode(databaseValue: Long): ClientPermissionResolution = when (databaseValue) {
                    0L -> ClientPermissionResolution.Permanent.Granted
                    1L -> ClientPermissionResolution.Permanent.Denied
                    2L -> ClientPermissionResolution.Denied
                    else -> throw Exception("unknown resolution code")
                }

                override fun encode(value: ClientPermissionResolution): Long = when (value) {
                    ClientPermissionResolution.Permanent.Granted -> 0L
                    ClientPermissionResolution.Permanent.Denied -> 1L
                    ClientPermissionResolution.Denied -> 2L
                }
            }
        )
    ).permissionResolutionsQueries

    override suspend fun retrieveResolutionFor(
        packageName: String
    ): ClientPermissionResolution? = withContext(ioDispatcher) {
        queries.resolutionFor(packageName).executeAsOneOrNull()
    }

    override suspend fun storeResolution(
        packageName: String,
        resolution: ClientPermissionResolution
    ) = withContext(ioDispatcher) {
        queries.storeResolution(packageName, resolution)
    }

    override fun resolutions(): Flow<List<Pair<String, ClientPermissionResolution>>> =
        queries.resolutions { packageName, resolution -> packageName to resolution }
            .asFlow()
            .mapToList(ioDispatcher)

    override suspend fun deleteResolutionFor(packageName: String) = withContext(ioDispatcher) {
        queries.deleteResolutionFor(packageName)
    }
}
