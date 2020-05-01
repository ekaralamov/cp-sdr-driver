package app.ekaralamov.sdr.driver.opening

import dagger.Subcomponent

object OpeningComponent {

    @Subcomponent(modules = [OperationsModule::class])
    interface Interface {

        fun injectOpenTuner(): OpenTuner
    }

    internal lateinit var instance: Interface

    fun setInstance(instance: Interface) {
        this.instance = instance
    }
}
