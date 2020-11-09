package sdr.driver.cp.onetuner

import android.app.Activity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import sdr.driver.cp.*

@RunWith(AndroidJUnit4::class)
class TwoClients {

    @get:Rule
    val buddy1Rule = BuddyRule.one()

    @get:Rule
    val buddy2Rule = BuddyRule.two()

    @Before
    fun clearPermissionsDB() {
        FakeClientPermissionStorage.clear()
    }

    @Test
    fun openingConflict() {
        buddy1Rule.getAccess(TunerOne.Device)
        buddy2Rule.getAccess(TunerOne.Device)

        val channel1 = buddy1Rule.buddy.openDataChannel(TunerOne.Device)
        assertThat(channel1).isNotNull()
        buddy2Rule.buddy.openCommandsChannel(TunerOne.Device).use { intruder ->
            assertThat(intruder).isNull()
        }
        DataStream(channel1).close()
    }

    @Test
    fun simultaneousPermissionRequests() {
        FakeClientPermissionStorage[buddy1Rule.packageName] = ClientPermissionResolution.Permanent.Granted
        FakeClientPermissionStorage[buddy2Rule.packageName] = ClientPermissionResolution.Permanent.Granted
        val getAccessRequest1Key = buddy1Rule.buddy.requestAccess(TunerOne.Device)
        val getAccessRequest2Key = buddy2Rule.buddy.requestAccess(TunerOne.Device)
        UsbDeviceAccessDialog.answerWithYes()
        assertThat(buddy1Rule.buddy.waitForResult(getAccessRequest1Key)).isEqualTo(Activity.RESULT_OK)
        assertThat(buddy2Rule.buddy.waitForResult(getAccessRequest2Key)).isEqualTo(Activity.RESULT_OK)
    }
}
