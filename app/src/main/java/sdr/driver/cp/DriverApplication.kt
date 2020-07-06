package sdr.driver.cp

import android.app.Application
import android.content.Context
import sdr.driver.cp.di.DaggerAppComponent
import sdr.driver.cp.opening.OpeningComponent
import sdr.driver.cp.opening.OpeningOperationsComponent
import sdr.driver.cp.permissions.PermissionsComponent
import sdr.driver.cp.permissions.PermissionsOperationsComponent
import timber.log.Timber

class DriverApplication : Application() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)

        Timber.plant(Timber.DebugTree())

        DaggerAppComponent.factory().create(base).apply {
            OpeningOperationsComponent.instance = injectOpeningOperationsComponent()
            OpeningComponent.setInstance(injectOpeningComponent())
            PermissionsComponent.setInstance(injectPermissionsComponent())
            PermissionsOperationsComponent.setInstance(injectPermissionsOperationsComponent())
        }.also {
            CommonOperationsComponent.setInstance(it)
        }
    }
}
