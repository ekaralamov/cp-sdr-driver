package sdr.driver.cp.permissions

import dagger.Subcomponent

object PermissionsComponent {

    @Subcomponent(
        modules = [
            OperationsModule::class,
            PresentationModule::class
        ]
    )
    interface Interface {

        fun injectGetTunerAccessViewModelFactory(): GetTunerAccessViewModel.Factory

        fun injectManagePermissionsViewModel(): ManagePermissionsViewModel
    }

    internal lateinit var instance: Interface

    fun setInstance(instance: Interface) {
        this.instance = instance
    }
}
