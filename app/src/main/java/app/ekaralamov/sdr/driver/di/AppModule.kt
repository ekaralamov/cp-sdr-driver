package app.ekaralamov.sdr.driver.di

import android.content.Context
import android.hardware.usb.UsbManager
import app.ekaralamov.sdr.driver.TunerAccessToken
import app.ekaralamov.sdr.driver.opening.OpeningOperationsComponent
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Named

@Module
object AppModule {

    @Provides
    fun Context.provideUsbManager(): UsbManager =
        getSystemService(Context.USB_SERVICE) as UsbManager

    @Provides
    @Named("IO")
    fun provideIODispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Named("Default")
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    fun provideAccessTokenRegistry(): TunerAccessToken.Registry<*, *> =
        OpeningOperationsComponent.instance.injectAccessTokenRegistry()
}
