package app.ekaralamov.sdr.driver.di

import android.content.Context
import app.ekaralamov.sdr.driver.opening.OpeningComponent
import app.ekaralamov.sdr.driver.opening.OpeningOperationsComponent
import app.ekaralamov.sdr.driver.opening.OpeningOperationsModule
import app.ekaralamov.sdr.driver.permissions.PermissionsComponent
import app.ekaralamov.sdr.driver.permissions.PermissionsOperationsComponent
import app.ekaralamov.sdr.driver.permissions.PermissionsOperationsModule
import app.ekaralamov.sdr.driver.permissions.PresentationModule
import dagger.BindsInstance
import dagger.Component

@Component(
    modules = [
        PresentationModule::class,
        AppModule::class,
        PermissionsOperationsModule::class,
        OpeningOperationsModule::class
    ]
)
interface AppComponent :
    PermissionsComponent.Interface,
    PermissionsOperationsComponent.Interface,
    OpeningComponent.Interface,
    OpeningOperationsComponent.Interface {

    @Component.Factory
    interface Factory {

        fun create(@BindsInstance context: Context): AppComponent
    }
}
