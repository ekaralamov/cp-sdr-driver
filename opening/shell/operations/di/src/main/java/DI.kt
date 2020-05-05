package app.ekaralamov.sdr.driver.opening

import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Module
object OperationsModule {

    @Provides
    fun providePlatformDeviceLocator(): PlatformDeviceLocator =
        OpeningOperationsComponent.instance.injectThePlatformDeviceLocator()
}

object OpeningOperationsComponent {

    @Subcomponent
    interface Interface {

        fun injectThePlatformDeviceLocator(): ThePlatformDeviceLocator
    }

    internal lateinit var instance: Interface

    fun setInstance(instance: Interface) {
        this.instance = instance
    }
}
