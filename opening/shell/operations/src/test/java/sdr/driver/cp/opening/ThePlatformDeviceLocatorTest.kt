package sdr.driver.cp.opening

import android.content.Context
import android.hardware.usb.UsbManager
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.kotest.assertions.throwables.shouldThrow
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowUsbManager
import java.io.FileNotFoundException

@RunWith(AndroidJUnit4::class)
class ThePlatformDeviceLocatorTest {

    private lateinit var usbManagerShadow: ShadowUsbManager
    private lateinit var sut: ThePlatformDeviceLocator

    @Before
    fun setUp() {
        val usbManager = getApplicationContext<Context>().getSystemService(Context.USB_SERVICE) as UsbManager
        usbManagerShadow = Shadows.shadowOf(usbManager)
        sut = ThePlatformDeviceLocator(usbManager)
    }

    @Test
    fun locatesTheDeviceWhenPresent() {
        with(usbManagerShadow) {
            addOrUpdateUsbDevice(
                mockk { every { deviceName } returns "another device path" },
                false
            )
            addOrUpdateUsbDevice(
                mockk { every { deviceName } returns "device path" },
                false
            )
        }

        assertThat(sut.getDeviceFor(DeviceAddress("device path")).deviceName).isEqualTo("device path")
    }

    @Test
    fun throwsFileNotFoundWhenDeviceNotPresent() {
        shouldThrow<FileNotFoundException> {
            sut.getDeviceFor(DeviceAddress("device path"))
        }
    }
}
