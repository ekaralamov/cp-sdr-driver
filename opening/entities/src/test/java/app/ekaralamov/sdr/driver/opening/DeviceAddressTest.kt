package app.ekaralamov.sdr.driver.opening

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.kotest.assertions.throwables.shouldThrow
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeviceAddressTest {

    @Test
    fun throwsIllegalArgumentForInvalidUri() {
        val uri = Uri.Builder().build()
        shouldThrow<IllegalArgumentException> {
            DeviceAddress.from(uri)
        }
    }
}
