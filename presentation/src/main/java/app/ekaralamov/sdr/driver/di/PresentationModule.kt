package app.ekaralamov.sdr.driver.di

import com.squareup.inject.assisted.dagger2.AssistedModule
import dagger.Module

@AssistedModule
@Module(includes = [AssistedInject_PresentationModule::class])
abstract class PresentationModule
