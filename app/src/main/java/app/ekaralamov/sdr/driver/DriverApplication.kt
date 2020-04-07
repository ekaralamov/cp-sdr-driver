package app.ekaralamov.sdr.driver

import android.app.Application

class DriverApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        internal lateinit var instance: DriverApplication
            private set
    }
}
