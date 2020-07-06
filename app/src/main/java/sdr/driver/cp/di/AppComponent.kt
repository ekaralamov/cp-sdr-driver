package sdr.driver.cp.di

import android.content.Context
import sdr.driver.cp.CommonOperationsComponent
import sdr.driver.cp.CommonOperationsModule
import sdr.driver.cp.opening.OpeningComponent
import sdr.driver.cp.opening.OpeningOperationsComponent
import sdr.driver.cp.permissions.PermissionsComponent
import sdr.driver.cp.permissions.PermissionsOperationsComponent
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
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
