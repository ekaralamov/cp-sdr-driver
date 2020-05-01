package app.ekaralamov.sdr.driver.opening

import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Module
object OperationsModule {

    @Provides
    fun provideDeviceLocator(): DeviceLocator =
        OpeningOperationsComponent.instance.injectTheDeviceLocator()
}

object OpeningOperationsComponent {

    @Subcomponent
    interface Interface {

        fun injectTheDeviceLocator(): TheDeviceLocator
    }

    internal lateinit var instance: Interface

    fun setInstance(instance: Interface) {
        this.instance = instance
    }
}
