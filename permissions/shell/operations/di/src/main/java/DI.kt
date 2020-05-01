package app.ekaralamov.sdr.driver.permissions

import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Module
object OperationsModule {

    @Provides
    fun provideDevicePermissionService(): DevicePermissionService =
        PermissionsOperationsComponent.instance.injectTheDevicePermissionService()
}

object PermissionsOperationsComponent {

    @Subcomponent
    interface Interface {

        fun injectTheDevicePermissionService(): TheDevicePermissionService
    }

    internal lateinit var instance: Interface

    fun setInstance(instance: Interface) {
        this.instance = instance
    }
}
