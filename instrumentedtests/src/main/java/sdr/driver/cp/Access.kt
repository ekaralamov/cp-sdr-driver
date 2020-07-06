package sdr.driver.cp

import android.app.Activity
import android.hardware.usb.UsbDevice
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObjectNotFoundException
import androidx.test.uiautomator.UiSelector
import sdr.driver.cp.test.buddy.Buddy
import com.google.common.truth.Truth.assertThat

object Access {

    fun get(tuner: UsbDevice, buddy: Buddy) {
        val getAccessRequestKey = buddy.requestAccess(tuner)
        val device = UiDevice.getInstance(getInstrumentation())
        val okPermissionButton =
            device.findObject(UiSelector().packageName("com.android.systemui").text("OK"))
        try {
            okPermissionButton.click()
        } catch (permissionPossiblyAlreadyGranted: UiObjectNotFoundException) {
        }
        assertThat(buddy.waitForResult(getAccessRequestKey)).isEqualTo(Activity.RESULT_OK)
    }
}
