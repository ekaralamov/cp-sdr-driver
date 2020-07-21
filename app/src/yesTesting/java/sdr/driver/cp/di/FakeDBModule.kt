package sdr.driver.cp.di

import dagger.Module
import dagger.Provides
import sdr.driver.cp.ClientPermissionStorage
import sdr.driver.cp.FakeClientPermissionStorage

@Module
object FakeDBModule {

    @Provides
    fun provideClientPermissionStorage(): ClientPermissionStorage = FakeClientPermissionStorage
}
