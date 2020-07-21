package sdr.driver.cp.onetuner

import android.app.Activity
import android.content.Intent
import android.os.ParcelFileDescriptor
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
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
            onView(withText("SDR Driver Test Buddy")).perform(click())
        }
        assertThat(FakeClientPermissionStorage[buddyRule.packageName]).isEqualTo(ClientPermissionResolution.Permanent.Granted)
    }

    @Test
    fun denyPermission() {
        buddyRule.getAccess(TunerOne.Device)
        val commandsChannel = buddyRule.buddy.openCommandsChannel(TunerOne.Device)
        val dataChannel = buddyRule.buddy.openDataChannel(TunerOne.Device)
        withManagePermissionsActivity {
            onView(withText("SDR Driver Test Buddy")).perform(click())
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
