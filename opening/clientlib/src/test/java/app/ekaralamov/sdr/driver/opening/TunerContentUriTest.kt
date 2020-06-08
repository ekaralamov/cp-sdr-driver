package app.ekaralamov.sdr.driver.opening

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TunerContentUriTest {

    @Test
    fun deviceAddressCodec() {
        val uri = TunerContentUri.build(
            mockk { every { deviceName } returns "/path/to/device" },
            ApplicationProvider.getApplicationContext()
        )
        assertThat(DeviceAddress.from(uri).path).isEqualTo("/path/to/device")
    }
}
