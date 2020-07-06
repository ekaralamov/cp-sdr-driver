package sdr.driver.cp.opening

import android.hardware.usb.UsbDevice
import sdr.driver.cp.TunerAccessToken
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Module(includes = [OperationsAssistModule::class])
object OperationsModule {

    @Provides
    fun providePlatformDeviceLocator(): PlatformDeviceLocator =
        OpeningOperationsComponent.instance.injectThePlatformDeviceLocator()
}

object OpeningOperationsComponent {

    @Subcomponent
    interface Interface {

        fun injectThePlatformDeviceLocator(): ThePlatformDeviceLocator

        fun injectAccessTokenRegistry(): TunerAccessToken.Registry<UsbDevice, TunerSession>
    }

    lateinit var instance: Interface
}
