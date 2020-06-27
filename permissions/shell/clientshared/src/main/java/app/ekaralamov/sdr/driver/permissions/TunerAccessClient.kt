package app.ekaralamov.sdr.driver.permissions

import android.app.Activity

object TunerAccessClient {

    object Extra {

        const val Device = "app.ekaralamov.sdr.driver.permissions.GetTunerAccessActivity.device"
    }

    object Result {

        const val IllegalArgument = Activity.RESULT_FIRST_USER
        const val Error = Activity.RESULT_FIRST_USER + 1
        const val DeviceAccessDenied = Activity.RESULT_FIRST_USER + 2
        const val ClientPermissionDenied = Activity.RESULT_FIRST_USER + 3
        const val ClientPermissionDeniedPermanently = Activity.RESULT_FIRST_USER + 4
    }
}
