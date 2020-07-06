package sdr.driver.cp

sealed class ClientPermissionResolution {

    object Denied : ClientPermissionResolution()

    sealed class Permanent : ClientPermissionResolution() {
        object Granted : Permanent()
        object Denied : Permanent()
    }
}
