package app.ekaralamov.sdr.driver.di

import android.content.Context
import android.hardware.usb.UsbManager
import dagger.Module
import dagger.Provides

@Module
object AppModule {

    @Provides
    fun Context.provideUsbManager(): UsbManager =
        getSystemService(Context.USB_SERVICE) as UsbManager
}
