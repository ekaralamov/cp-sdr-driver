package app.ekaralamov.sdr.driver.permissions

import dagger.Module
import dagger.Provides

@Module
object PermissionsOperationsModule {

    @Provides
    fun provideDevicePermissionService(): DevicePermissionService =
        PermissionsOperationsComponent.instance.injectTheDevicePermissionService()
}

object PermissionsOperationsComponent {

    interface Interface {

        fun injectTheDevicePermissionService(): TheDevicePermissionService
    }

    internal lateinit var instance: Interface

    fun setInstance(instance: Interface) {
        this.instance = instance
    }
}
