package app.ekaralamov.sdr.driver

sealed class ClientPermissionResolution {

    object Denied : ClientPermissionResolution()

    sealed class Permanent : ClientPermissionResolution() {
        object Granted : Permanent()
        object Denied : Permanent()
    }
}
