package app.ekaralamov.sdr.driver

import android.app.Activity

const val GetTunerAccessDeviceExtra =
    "app.ekaralamov.sdr.driver.GetTunerAccessActivity.device"

object GetTunerAccessResult {
    const val AccessDenied = Activity.RESULT_FIRST_USER
    const val IllegalArgument = Activity.RESULT_FIRST_USER + 1
}
