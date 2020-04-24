package app.ekaralamov.sdr.driver

import android.app.Application
import app.ekaralamov.sdr.driver.di.AppComponent
import app.ekaralamov.sdr.driver.di.DaggerAppComponent

class DriverApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this

        appComponent = DaggerAppComponent.create()
    }

    companion object {
        internal lateinit var instance: DriverApplication
            private set

        lateinit var appComponent: AppComponent
            private set
    }
}
