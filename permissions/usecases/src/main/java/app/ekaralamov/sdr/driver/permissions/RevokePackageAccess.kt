package app.ekaralamov.sdr.driver.permissions

import app.ekaralamov.sdr.driver.TunerAccessToken
import javax.inject.Inject

class RevokePackageAccess @Inject constructor(
    private val accessTokenRegistry: TunerAccessToken.Registry<*, *>
) {

    suspend operator fun invoke(packageName: String) {
    }
}
