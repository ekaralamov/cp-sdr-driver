package app.ekaralamov.sdr.driver

import android.app.Application
import app.ekaralamov.sdr.driver.di.DaggerAppComponent
import app.ekaralamov.sdr.driver.opening.OpeningComponent
import app.ekaralamov.sdr.driver.opening.OpeningOperationsComponent
import app.ekaralamov.sdr.driver.permissions.PermissionsComponent
import app.ekaralamov.sdr.driver.permissions.PermissionsOperationsComponent
import timber.log.Timber

class DriverApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())

        DaggerAppComponent.factory().create(this).apply {
            OpeningOperationsComponent.setInstance(injectOpeningOperationsComponent())
            OpeningComponent.setInstance(injectOpeningComponent())
            PermissionsComponent.setInstance(injectPermissionsComponent())
            PermissionsOperationsComponent.setInstance(injectPermissionsOperationsComponent())
        }.also {
            CommonOperationsComponent.setInstance(it)
        }
    }
}
