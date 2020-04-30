package app.ekaralamov.sdr.driver.opening

import dagger.Module
import dagger.Provides

@Module
object OpeningOperationsModule {

    @Provides
    fun provideDeviceLocator(): DeviceLocator =
        OpeningOperationsComponent.instance.injectTheDeviceLocator()
}

object OpeningOperationsComponent {

    interface Interface {

        fun injectTheDeviceLocator(): TheDeviceLocator
    }

    internal lateinit var instance: Interface

    fun setInstance(instance: Interface) {
        this.instance = instance
    }
}
