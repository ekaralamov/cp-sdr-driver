package sdr.driver.cp

import dagger.Module
import dagger.Provides

@Module
object CommonOperationsModule {

    @Provides
    fun provideClientPermissionStorage(): ClientPermissionStorage =
        CommonOperationsComponent.instance.injectTheClientPermissionStorage()
}

object CommonOperationsComponent {

    interface Interface {

        fun injectTheClientPermissionStorage(): TheClientPermissionStorage
    }

    internal lateinit var instance: Interface

    fun setInstance(instance: Interface) {
        this.instance = instance
    }
}
