package app.ekaralamov.sdr.driver.di

import app.ekaralamov.sdr.driver.DevicePermissionService
import app.ekaralamov.sdr.driver.DriverApplication
import dagger.Module
import dagger.Provides

@Module
object AppModule {

    @Provides
    fun provideDevicePermissionService(): DevicePermissionService =
        DriverApplication.appComponent.injectTheDevicePermissionService()
}
