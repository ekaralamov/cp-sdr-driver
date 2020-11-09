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
import sdr.driver.cp.BuddyRule
import sdr.driver.cp.Commands
import sdr.driver.cp.DataStream
import sdr.driver.cp.FakeClientPermissionStorage

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

            DataStream(buddyRule.buddy.openDataChannel(TunerOne.Device)).close()
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
        DataStream(dataChannel1).close()
        runBlocking { DataStream(dataChannel2.await()).close() }
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

    @Test
    fun priming() {
        buddyRule.getAccess(TunerOne.Device)
        DataStream(buddyRule.buddy.openDataChannel(TunerOne.Device)).use { dataStream ->
            ParcelFileDescriptor.AutoCloseOutputStream(buddyRule.buddy.openCommandsChannel(TunerOne.Device))
                .use { commandsStream ->
                    dataStream.assertNotActive()
                    commandsStream.write(Commands.Frequency)
                    dataStream.assertNotActive()
                    commandsStream.write(Commands.SampleRate)
                    dataStream.assertActive()
                }
        }
    }
}
