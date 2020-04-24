package app.ekaralamov.sdr.driver.di

import app.ekaralamov.sdr.driver.GetTunerAccessViewModel
import app.ekaralamov.sdr.driver.TheDevicePermissionService
import dagger.Component

@Component(
    modules = [
        PresentationModule::class, AppModule::class
    ]
)
interface AppComponent {

    fun injectGetTunerAccessViewModelFactory(): GetTunerAccessViewModel.Factory

    fun injectTheDevicePermissionService(): TheDevicePermissionService
}
