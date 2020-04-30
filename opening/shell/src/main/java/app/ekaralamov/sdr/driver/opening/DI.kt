package app.ekaralamov.sdr.driver.opening

object OpeningComponent {

    interface Interface {

        fun injectOpenTuner(): OpenTuner
    }

    internal lateinit var instance: Interface

    fun setInstance(instance: Interface) {
        this.instance = instance
    }
}
