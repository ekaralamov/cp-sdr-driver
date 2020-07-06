package sdr.driver.cp.onetuner

import android.app.Activity
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObjectNotFoundException
import androidx.test.uiautomator.UiSelector
import sdr.driver.cp.Access
import sdr.driver.cp.BuddyRule
import sdr.driver.cp.Data
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TwoClients {

    @get:Rule
    val buddy1Rule = BuddyRule.one()

    @get:Rule
    val buddy2Rule = BuddyRule.two()

    @Test
    fun openingConflict() {
        Access.get(TunerOne.Device, buddy1Rule.buddy)
        Access.get(TunerOne.Device, buddy2Rule.buddy)

        val channel1 = buddy1Rule.buddy.openDataChannel(TunerOne.Device)
        assertThat(channel1).isNotNull()
        buddy2Rule.buddy.openCommandsChannel(TunerOne.Device).use { intruder ->
            assertThat(intruder).isNull()
        }
        Data.useChannel(channel1)
    }

    @Test
    fun simultaneousPermissionRequests() {
        val getAccessRequest1Key = buddy1Rule.buddy.requestAccess(TunerOne.Device)
        val getAccessRequest2Key = buddy2Rule.buddy.requestAccess(TunerOne.Device)
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val okPermissionButton =
            device.findObject(UiSelector().packageName("com.android.systemui").text("OK"))
        try {
            okPermissionButton.click()
        } catch (permissionPossiblyAlreadyGranted: UiObjectNotFoundException) {
        }
        assertThat(buddy1Rule.buddy.waitForResult(getAccessRequest1Key)).isEqualTo(Activity.RESULT_OK)
        assertThat(buddy2Rule.buddy.waitForResult(getAccessRequest2Key)).isEqualTo(Activity.RESULT_OK)
    }
}
