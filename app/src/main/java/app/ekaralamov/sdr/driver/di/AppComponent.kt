package app.ekaralamov.sdr.driver.di

import android.content.Context
import app.ekaralamov.sdr.driver.CommonOperationsComponent
import app.ekaralamov.sdr.driver.CommonOperationsModule
import app.ekaralamov.sdr.driver.opening.OpeningComponent
import app.ekaralamov.sdr.driver.opening.OpeningOperationsComponent
import app.ekaralamov.sdr.driver.permissions.PermissionsComponent
import app.ekaralamov.sdr.driver.permissions.PermissionsOperationsComponent
import dagger.BindsInstance
import dagger.Component

@Component(modules = [AppModule::class, CommonOperationsModule::class])
interface AppComponent : CommonOperationsComponent.Interface {

    @Component.Factory
    interface Factory {

        fun create(@BindsInstance context: Context): AppComponent
    }

    fun injectPermissionsOperationsComponent(): PermissionsOperationsComponent.Interface
    fun injectPermissionsComponent(): PermissionsComponent.Interface
    fun injectOpeningComponent(): OpeningComponent.Interface
    fun injectOpeningOperationsComponent(): OpeningOperationsComponent.Interface
}
