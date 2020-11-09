package sdr.driver.cp

import android.os.ParcelFileDescriptor
import com.google.common.truth.Truth.assertThat
import java.io.Closeable
import java.io.DataInputStream

class DataStream(dataChannel: ParcelFileDescriptor) : Closeable {

    private val dataInputStream = DataInputStream(ParcelFileDescriptor.AutoCloseInputStream(dataChannel))

    init {
        val dataHead = ByteArray(Head.size)
        dataInputStream.readFully(dataHead)
        assertThat(dataHead).isEqualTo(Head)
    }

    override fun close() = dataInputStream.close()

    fun assertNotActive() {
        Thread.sleep(200)
        assertThat(dataInputStream.available()).isLessThan(2000)
    }

    fun assertActive() {
        val buffer = ByteArray(1024 * 1024)
        dataInputStream.readFully(buffer)
    }

    companion object {

        val Head = "RTL0".toByteArray(Charsets.US_ASCII)
    }
}
