package app.ekaralamov.sdr.driver.onetuner

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.ekaralamov.sdr.driver.Access
import app.ekaralamov.sdr.driver.BuddyRule
import app.ekaralamov.sdr.driver.Data
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
    fun conflict() {
        Access.get(TunerOne.Device, buddy1Rule.buddy)
        Access.get(TunerOne.Device, buddy2Rule.buddy)

        val channel1 = buddy1Rule.buddy.openDataChannel(TunerOne.Device)
        assertThat(channel1).isNotNull()
        buddy2Rule.buddy.openCommandsChannel(TunerOne.Device).use { intruder ->
            assertThat(intruder).isNull()
        }
        Data.useChannel(channel1)
    }
}
