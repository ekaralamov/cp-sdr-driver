package app.ekaralamov.sdr.driver.permissions

import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.ekaralamov.CoroutineTestRule
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows

@RunWith(AndroidJUnit4::class)
class TheDevicePermissionServiceTest {

    private lateinit var context: Application
    private lateinit var pendingIntentSlot: CapturingSlot<PendingIntent>

    private lateinit var sut: TheDevicePermissionService

    private val device = mockk<UsbDevice> {
        every { vendorId } returns 1
        every { productId } returns 2
    }

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    @Before
    fun setUp() {
        context = getApplicationContext()
        pendingIntentSlot = slot()
        val usbManager = mockk<UsbManager> {
            every { requestPermission(device, capture(pendingIntentSlot)) } just Runs
        }

        sut = TheDevicePermissionService(context, usbManager)
    }

    @Test
    fun permissionGranted() {
        val testContainer = coroutineTestRule.run {
            sut.getDevicePermission(device)
        }
        pendingIntentSlot.captured.send(context, 0, Intent().apply {
            putExtra(UsbManager.EXTRA_DEVICE, device)
            putExtra(UsbManager.EXTRA_PERMISSION_GRANTED, true)
        })
        assertThat(testContainer.getResult()).isTrue()
    }

    @Test
    fun permissionDenied() {
        val testContainer = coroutineTestRule.run {
            sut.getDevicePermission(device)
        }
        pendingIntentSlot.captured.send(context, 0, Intent().apply {
            putExtra(UsbManager.EXTRA_DEVICE, device)
            putExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
        })
        assertThat(testContainer.getResult()).isFalse()
    }

    @Test
    fun anotherDevice() {
        val testContainer = coroutineTestRule.run {
            sut.getDevicePermission(device)
        }
        pendingIntentSlot.captured.send(context, 0, Intent().apply {
            putExtra(UsbManager.EXTRA_DEVICE, mockk<UsbDevice>())
            putExtra(UsbManager.EXTRA_PERMISSION_GRANTED, true)
        })
        assertThat(testContainer.hasCompleted).isFalse()
    }

    @Test
    fun cancel() {
        val testContainer = coroutineTestRule.run {
            sut.getDevicePermission(device)
        }
        testContainer.cancel()
        assertThat(Shadows.shadowOf(context).registeredReceivers).isEmpty()
    }
}
