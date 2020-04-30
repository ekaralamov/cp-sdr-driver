package app.ekaralamov.sdr.driver.permissions

object PermissionsComponent {

    interface Interface {

        fun injectGetTunerAccessViewModelFactory(): GetTunerAccessViewModel.Factory
    }

    internal lateinit var instance: Interface

    fun setInstance(instance: Interface) {
        this.instance = instance
    }
}
