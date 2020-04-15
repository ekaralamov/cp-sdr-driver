package app.ekaralamov.sdr.driver.onetuner

import android.app.Activity
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.ekaralamov.sdr.driver.BuddyRule
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EndToEndTest {

    @get:Rule
    val buddyRule = BuddyRule()

    @Test
    fun openTunerOne() {
//        val uri = buildContentUri(TunerOneId)
//        InstrumentationRegistry.getInstrumentation().targetContext.grantUriPermission(
//            "app.ekaralamov.sdr.driver.test.buddy",
//            uri,
//            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
//        )

        val getAccessRequestKey = buddyRule.buddy.requestAccess(TunerOne.DeviceName)

        val getAccessResult = buddyRule.buddy.waitForAccess(getAccessRequestKey)

        assertThat(getAccessResult, equalTo(Activity.RESULT_OK))
    }
}
