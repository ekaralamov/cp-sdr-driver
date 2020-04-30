package app.ekaralamov.sdr.driver

import android.app.Application
import app.ekaralamov.sdr.driver.di.DaggerAppComponent
import app.ekaralamov.sdr.driver.opening.OpeningComponent
import app.ekaralamov.sdr.driver.opening.OpeningOperationsComponent
import app.ekaralamov.sdr.driver.permissions.PermissionsComponent
import app.ekaralamov.sdr.driver.permissions.PermissionsOperationsComponent

class DriverApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        DaggerAppComponent.factory().create(this).let {
            OpeningOperationsComponent.setInstance(it)
            OpeningComponent.setInstance(it)
            PermissionsComponent.setInstance(it)
            PermissionsOperationsComponent.setInstance(it)
        }
    }
}
