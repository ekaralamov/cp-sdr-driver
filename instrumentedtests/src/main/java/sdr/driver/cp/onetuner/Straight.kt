package sdr.driver.cp.onetuner

import android.os.ParcelFileDescriptor
import androidx.test.ext.junit.runners.AndroidJUnit4
import sdr.driver.cp.Access
import sdr.driver.cp.BuddyRule
import sdr.driver.cp.Commands
import sdr.driver.cp.Data
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Straight {

    @get:Rule
    val buddyRule = BuddyRule.one()

    @Test
    fun permissionGranted() {
        Access.get(TunerOne.Device, buddyRule.buddy)

        val commandsChannel1 = buddyRule.buddy.openCommandsChannel(TunerOne.Device)
        val commandsChannel2 = GlobalScope.async(Dispatchers.IO) {
            buddyRule.buddy.openCommandsChannel(TunerOne.Device)
        }

        ParcelFileDescriptor.AutoCloseOutputStream(commandsChannel1).use {
            it.write(Commands.Ignored)

            Data.useChannel(buddyRule.buddy.openDataChannel(TunerOne.Device))
        }

        runBlocking {
            ParcelFileDescriptor.AutoCloseOutputStream(commandsChannel2.await()).use {
                it.write(Commands.Ignored)
            }
        }

        val dataChannel1 = buddyRule.buddy.openDataChannel(TunerOne.Device)
        val dataChannel2 = GlobalScope.async(Dispatchers.IO) {
            buddyRule.buddy.openDataChannel(TunerOne.Device)
        }
        Data.useChannel(dataChannel1)
        runBlocking { Data.useChannel(dataChannel2.await()) }
    }
}
