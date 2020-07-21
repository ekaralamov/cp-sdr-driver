package sdr.driver.cp.twotuners

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
import sdr.driver.cp.onetuner.TunerOne

@RunWith(AndroidJUnit4::class)
class Opening {

    @get:Rule
    val buddyRule = BuddyRule.one()

    @Before
    fun clearPermissionsDB() {
        FakeClientPermissionStorage.clear()
    }

    @Test
    fun openTwoTuners() {
        buddyRule.getAccess(TunerOne.Device)
        buddyRule.getAccess(TunerTwo.Device)

        val commandsChannel1 = buddyRule.buddy.openCommandsChannel(TunerOne.Device)
        val commandsChannel2 = buddyRule.buddy.openCommandsChannel(TunerTwo.Device)

        ParcelFileDescriptor.AutoCloseOutputStream(commandsChannel1).use { channel1CommandStream ->
            channel1CommandStream.write(Commands.Ignored)

            ParcelFileDescriptor.AutoCloseOutputStream(commandsChannel2).use { channel2CommandStream ->
                channel2CommandStream.write(Commands.Ignored)
            }
        }
    }
}
