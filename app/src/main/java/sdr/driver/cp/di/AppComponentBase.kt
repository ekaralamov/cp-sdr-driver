package sdr.driver.cp.di

import sdr.driver.cp.CommonOperationsComponent
import sdr.driver.cp.opening.OpeningComponent
import sdr.driver.cp.opening.OpeningOperationsComponent
import sdr.driver.cp.permissions.PermissionsComponent
import sdr.driver.cp.permissions.PermissionsOperationsComponent

interface AppComponentBase : CommonOperationsComponent.Interface {

    fun injectPermissionsOperationsComponent(): PermissionsOperationsComponent.Interface
    fun injectPermissionsComponent(): PermissionsComponent.Interface
    fun injectOpeningComponent(): OpeningComponent.Interface
    fun injectOpeningOperationsComponent(): OpeningOperationsComponent.Interface
}
