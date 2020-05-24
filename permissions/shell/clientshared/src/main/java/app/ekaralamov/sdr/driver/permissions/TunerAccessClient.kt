package app.ekaralamov.sdr.driver.permissions

import android.app.Activity

object TunerAccessClient {

    const val DeviceExtra =
        "app.ekaralamov.sdr.driver.permissions.GetTunerAccessActivity.device"

    object Result {
        const val AccessDenied = Activity.RESULT_FIRST_USER
        const val IllegalArgument = Activity.RESULT_FIRST_USER + 1
    }
}
