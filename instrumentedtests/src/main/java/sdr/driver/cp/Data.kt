package sdr.driver.cp

import android.os.ParcelFileDescriptor
import com.google.common.truth.Truth.assertThat
import java.io.DataInputStream

object Data {

    val Head = "RTL0".toByteArray(Charsets.US_ASCII)

    fun useChannel(dataChannel: ParcelFileDescriptor) {
        val dataHead = ByteArray(Head.size)
        val buffer = ByteArray(1024 * 1024)
        DataInputStream(ParcelFileDescriptor.AutoCloseInputStream(dataChannel)).use {
            it.readFully(dataHead)
            assertThat(dataHead).isEqualTo(Head)
            it.readFully(buffer)
        }
    }
}
