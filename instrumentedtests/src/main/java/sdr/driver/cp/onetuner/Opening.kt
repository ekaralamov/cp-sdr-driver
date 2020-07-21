package sdr.driver.cp.onetuner

import android.os.ParcelFileDescriptor
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import sdr.driver.cp.*

@RunWith(AndroidJUnit4::class)
class Opening {

    @get:Rule
    val buddyRule = BuddyRule.one()

    @Before
    fun clearPermissionsDB() {
        FakeClientPermissionStorage.clear()
    }

    @Test
    fun multipleChannelOpenings() {
        buddyRule.getAccess(TunerOne.Device)

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

    @Test
    fun clientPermissionNotGranted() {
        assertThrows(SecurityException::class.java) {
            buddyRule.buddy.openCommandsChannel(TunerOne.Device)
        }
        assertThrows(SecurityException::class.java) {
            buddyRule.buddy.openDataChannel(TunerOne.Device)
        }
    }
}
