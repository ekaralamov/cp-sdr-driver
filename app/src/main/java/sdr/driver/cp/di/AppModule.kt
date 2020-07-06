package sdr.driver.cp.di

import android.content.Context
import android.hardware.usb.UsbManager
import sdr.driver.cp.TunerAccessToken
import sdr.driver.cp.opening.OpeningOperationsComponent
import sdr.driver.cp.operations.shell.Database
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Named
import javax.inject.Singleton

@Module
object AppModule {

    @Provides
    fun Context.provideUsbManager(): UsbManager =
        getSystemService(Context.USB_SERVICE) as UsbManager

    @Provides
    @Named("IO")
    fun provideIODispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Named("default")
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    fun provideAccessTokenRegistry(): TunerAccessToken.Registry<*, *> =
        OpeningOperationsComponent.instance.injectAccessTokenRegistry()

    @Singleton
    @Provides
    @Named("permissions")
    fun providePermissionsSqlDriver(context: Context): SqlDriver =
        AndroidSqliteDriver(Database.Schema, context, "permissions.db")
}
