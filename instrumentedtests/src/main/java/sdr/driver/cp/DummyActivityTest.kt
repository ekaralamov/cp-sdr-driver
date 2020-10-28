package sdr.driver.cp

import android.content.Intent
import android.hardware.usb.UsbManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.UiDevice
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DummyActivityTest {

    @Test
    fun doesNotBringToFrontMainTask() {
        launchMainActivity()
        assertThat(UiDevice.getInstance(getInstrumentation()).currentPackageName).isEqualTo("sdr.driver.cp")
        pressHome()
        launchDummyActivity()
        waitForSystemToEventuallyBringTaskToFront()
        assertThat(UiDevice.getInstance(getInstrumentation()).currentPackageName).isNotEqualTo("sdr.driver.cp")
    }
}

private fun launchMainActivity() = getInstrumentation().context.startActivity(
    Intent().apply {
        setClassName(getInstrumentation().targetContext, "sdr.driver.cp.MainActivity")
        action = Intent.ACTION_MAIN
        addCategory(Intent.CATEGORY_LAUNCHER)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
)

private fun launchDummyActivity() = getInstrumentation().context.startActivity(
    Intent().apply {
        setClassName(getInstrumentation().targetContext, "sdr.driver.cp.permissions.DummyActivity")
        action = UsbManager.ACTION_USB_DEVICE_ATTACHED
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
)

private fun pressHome() = assertThat(UiDevice.getInstance(getInstrumentation()).pressHome()).isTrue()

private fun waitForSystemToEventuallyBringTaskToFront() = Thread.sleep(10_000)
