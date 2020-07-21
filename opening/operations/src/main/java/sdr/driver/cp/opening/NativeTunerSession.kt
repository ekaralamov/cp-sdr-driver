package sdr.driver.cp.opening

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject

class NativeTunerSession @AssistedInject constructor(
    @Assisted device: UsbDevice,
    usbManager: UsbManager
) {

    @AssistedInject.Factory
    interface Factory {

        fun create(device: UsbDevice): NativeTunerSession
    }

    private val connection: UsbDeviceConnection = usbManager.openDevice(device) ?: throw Exception()
    private val nativeHandle = NativeCalls.open(connection.fileDescriptor).also {
        if (it <= 0) {
            connection.close()
            throw Exception()
        }
    }

    fun pumpCommands(inputFD: Int) = NativeCalls.pumpCommands(nativeHandle, inputFD)

    fun pumpData(outputFD: Int) = NativeCalls.pumpData(nativeHandle, outputFD)

    fun stopPumps() = NativeCalls.stopPumps(nativeHandle)

    fun close() {
        NativeCalls.close(nativeHandle)
        connection.close()
    }
}
