package sdr.driver.cp.opening

import com.squareup.inject.assisted.dagger2.AssistedModule
import dagger.Module

@AssistedModule
@Module(includes = [AssistedInject_OperationsAssistModule::class])
object OperationsAssistModule
