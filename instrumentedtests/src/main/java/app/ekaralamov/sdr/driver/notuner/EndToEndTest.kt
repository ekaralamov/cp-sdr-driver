package app.ekaralamov.sdr.driver.notuner

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.ekaralamov.sdr.driver.BuddyRule
import app.ekaralamov.sdr.driver.GetTunerAccessResult
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
    fun getAccessNonExistentDevice() {
        val requestKey = buddyRule.buddy.requestAccess("non-existent device name")

        val result = buddyRule.buddy.waitForAccess(requestKey)

        assertThat(result, equalTo(GetTunerAccessResult.DeviceNotFound))
    }
}
