package sdr.driver.cp.onetuner

import android.app.Activity
import android.content.Intent
import android.os.ParcelFileDescriptor
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import sdr.driver.cp.BuddyRule
import sdr.driver.cp.ClientPermissionResolution
import sdr.driver.cp.Commands
import sdr.driver.cp.FakeClientPermissionStorage
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class ManagePermissions {

    @get:Rule
    val buddyRule = BuddyRule.one()

    @Before
    fun clearPermissionsDB() {
        FakeClientPermissionStorage.clear()
    }

    @Test
    fun grantPermission() {
        FakeClientPermissionStorage[buddyRule.packageName] = ClientPermissionResolution.Permanent.Denied
        FakeClientPermissionStorage["some.uninstalled.package"] = ClientPermissionResolution.Permanent.Denied
        withManagePermissionsActivity {
            // could be done via Espresso once https://github.com/Kotlin/kotlinx.coroutines/issues/242 is fixed
            UiDevice.getInstance(getInstrumentation()).findObject(UiSelector().textContains("SDR Driver Test Buddy")).click()
        }
        assertThat(FakeClientPermissionStorage[buddyRule.packageName]).isEqualTo(ClientPermissionResolution.Permanent.Granted)
    }

    @Test
    fun denyPermission() {
        buddyRule.getAccess(TunerOne.Device)
        val commandsChannel = buddyRule.buddy.openCommandsChannel(TunerOne.Device)
        val dataChannel = buddyRule.buddy.openDataChannel(TunerOne.Device)
        withManagePermissionsActivity {
            // could be done via Espresso once https://github.com/Kotlin/kotlinx.coroutines/issues/242 is fixed
            UiDevice.getInstance(getInstrumentation()).findObject(UiSelector().textContains("SDR Driver Test Buddy")).click()
        }
        assertThat(FakeClientPermissionStorage[buddyRule.packageName]).isEqualTo(ClientPermissionResolution.Permanent.Denied)
        assertThrows(IOException::class.java) {
            ParcelFileDescriptor.AutoCloseOutputStream(commandsChannel).use {
                it.write(Commands.Ignored)
            }
        }
        ParcelFileDescriptor.AutoCloseInputStream(dataChannel).use {
            @Suppress("ControlFlowWithEmptyBody")
            while (it.read() >= 0);
        }
    }

    private inline fun withManagePermissionsActivity(block: (ActivityScenario<Activity>) -> Unit) =
        ActivityScenario.launch<Activity>(
            Intent().setClassName(getInstrumentation().targetContext, "sdr.driver.cp.permissions.ManagePermissionsActivity")
        ).use(block)
}
