package sdr.driver.cp.onetuner

import android.app.Activity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import sdr.driver.cp.*
import sdr.driver.cp.permissions.TunerAccessClient

@RunWith(AndroidJUnit4::class)
class GetAccess {

    @get:Rule
    val buddyRule = BuddyRule.one()

    @Before
    fun clearPermissionsDB() {
        FakeClientPermissionStorage.clear()
    }

    @Test
    fun grantPermissionGivenNoPreviousResolution() {
        grantPermission()
    }

    @Test
    fun grantPermissionGivenPreviousDenial() {
        FakeClientPermissionStorage[buddyRule.packageName] = ClientPermissionResolution.Denied
        grantPermission()
    }

    private fun grantPermission() {
        val getAccessRequestKey = buddyRule.buddy.requestAccess(TunerOne.Device)
        AlertDialog.Button.Positive.click()
        UsbDeviceAccessDialog.answerWithYes()
        assertThat(buddyRule.buddy.waitForResult(getAccessRequestKey)).isEqualTo(Activity.RESULT_OK)
        assertThat(FakeClientPermissionStorage[buddyRule.packageName]).isEqualTo(ClientPermissionResolution.Permanent.Granted)
    }

    @Test
    fun denyPermissionGivenNoPreviousResolution() {
        denyPermission()
    }

    @Test
    fun denyPermissionGivenPreviousDenial() {
        FakeClientPermissionStorage[buddyRule.packageName] = ClientPermissionResolution.Denied
        denyPermission()
    }

    private fun denyPermission() {
        val getAccessRequestKey = buddyRule.buddy.requestAccess(TunerOne.Device)
        AlertDialog.Button.Neutral.click()
        assertThat(buddyRule.buddy.waitForResult(getAccessRequestKey)).isEqualTo(TunerAccessClient.Result.ClientPermissionDenied)
        assertThat(FakeClientPermissionStorage[buddyRule.packageName]).isEqualTo(ClientPermissionResolution.Denied)
    }

    @Test
    fun permanentlyDenyPermission() {
        FakeClientPermissionStorage[buddyRule.packageName] = ClientPermissionResolution.Denied
        val getAccessRequestKey = buddyRule.buddy.requestAccess(TunerOne.Device)
        AlertDialog.Button.Negative.click()
        assertThat(buddyRule.buddy.waitForResult(getAccessRequestKey)).isEqualTo(TunerAccessClient.Result.ClientPermissionDeniedPermanently)
        assertThat(FakeClientPermissionStorage[buddyRule.packageName]).isEqualTo(ClientPermissionResolution.Permanent.Denied)
    }

    @Test
    fun permissionAlreadyPermanentlyDenied() {
        FakeClientPermissionStorage[buddyRule.packageName] = ClientPermissionResolution.Permanent.Denied
        val getAccessRequestKey = buddyRule.buddy.requestAccess(TunerOne.Device)
        assertThat(buddyRule.buddy.waitForResult(getAccessRequestKey)).isEqualTo(TunerAccessClient.Result.ClientPermissionDeniedPermanently)
    }
}
