package app.ekaralamov.sdr.driver.permissions

import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Module
object OperationsModule {

    @Provides
    fun providePlatformDevicePermissionAuthority(): PlatformDevicePermissionAuthority =
        PermissionsOperationsComponent.instance.injectThePlatformDevicePermissionAuthority()
}

object PermissionsOperationsComponent {

    @Subcomponent
    interface Interface {

        fun injectThePlatformDevicePermissionAuthority(): ThePlatformDevicePermissionAuthority
    }

    internal lateinit var instance: Interface

    fun setInstance(instance: Interface) {
        this.instance = instance
    }
}
