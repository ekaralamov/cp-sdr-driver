package app.ekaralamov.sdr.driver.opening

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.os.ParcelFileDescriptor
import app.ekaralamov.sdr.driver.PermissionRevokedException
import app.ekaralamov.sdr.driver.TunerAccessToken
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.*
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Named
import kotlin.concurrent.withLock

class TunerSession @AssistedInject constructor(
    @Assisted device: UsbDevice,
    usbManager: UsbManager,
    @Named("IO") private val ioDispatcher: CoroutineDispatcher
) : TunerAccessToken.Session {

    @AssistedInject.Factory
    interface Factory {

        fun create(device: UsbDevice): TunerSession
    }

    private val connection: UsbDeviceConnection = usbManager.openDevice(device) ?: throw Exception()
    private var nativeHandle = NativeCalls.open(connection.fileDescriptor).also {
        if (it <= 0) {
            connection.close()
            throw Exception()
        }
    }
    private val lock = ReentrantLock()

    private var commandsPipe: Pipe? = null
    private var commandsPumping: Job? = null
    private val commandsClosed = lock.newCondition()

    private var dataPipe: Pipe? = null
    private var dataPumping: Job? = null
    private val dataClosed = lock.newCondition()

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun close() {
        var savedNativeHandle = 0L
        lock.withLock {
            if (nativeHandle == 0L)
                throw AssertionError("closing should be managed by TunerAccessToken and should happen only once")
            savedNativeHandle = nativeHandle
            nativeHandle = 0L
        }

        try {
            NativeCalls.stopDataPump(savedNativeHandle)
            dataPipe?.close()
            commandsPipe?.close()

            try {
                commandsPumping?.join()
            } catch (cancellationException: CancellationException) {
                // ignore
            }
            try {
                dataPumping?.join()
            } catch (cancellationException: CancellationException) {
                // ignore
            }

            NativeCalls.close(savedNativeHandle)
            connection.close()
        } catch (throwable: Throwable) {
            throw AssertionError(throwable)
        }
    }

    fun startCommandsPump(accessToken: TunerAccessToken<UsbDevice, TunerSession>): ParcelFileDescriptor =
        lock.withLock {
            try {
                if (nativeHandle == 0L) throw PermissionRevokedException()
                if (commandsPipe != null) commandsClosed.await()
                commandsPipe = Pipe()
                commandsPumping = GlobalScope.launch(ioDispatcher) {
                    try {
                        NativeCalls.pumpCommands(
                            nativeSessionHandle = nativeHandle,
                            inputFD = commandsPipe!!.input.fd
                        )
                    } finally {
                        commandsPumping!!.cancel()
                        accessToken.release()

                        commandsPipe!!.close()
                        lock.withLock {
                            commandsPipe = null
                            commandsClosed.signal()
                        }
                    }
                }
                commandsPipe!!.output
            } catch (throwable: Throwable) {
                GlobalScope.launch { accessToken.release() }
                throw throwable
            }
        }

    fun startDataPump(accessToken: TunerAccessToken<UsbDevice, TunerSession>): ParcelFileDescriptor =
        lock.withLock {
            try {
                if (nativeHandle == 0L) throw PermissionRevokedException()
                if (dataPipe != null) dataClosed.await()
                dataPipe = Pipe()
                dataPumping = GlobalScope.launch(ioDispatcher) {
                    try {
                        NativeCalls.pumpData(
                            nativeSessionHandle = nativeHandle,
                            outputFD = dataPipe!!.output.fd
                        )
                    } finally {
                        dataPumping!!.cancel()
                        accessToken.release()

                        dataPipe!!.close()
                        lock.withLock {
                            dataPipe = null
                            dataClosed.signal()
                        }
                    }
                }
                dataPipe!!.input
            } catch (throwable: Throwable) {
                GlobalScope.launch { accessToken.release() }
                throw throwable
            }
        }
}

private inline class Pipe(private val descriptors: Array<ParcelFileDescriptor>) {

    constructor() : this(ParcelFileDescriptor.createPipe())

    val input: ParcelFileDescriptor
        get() = descriptors[0]

    val output: ParcelFileDescriptor
        get() = descriptors[1]

    fun close() {
        input.close()
        output.close()
    }
}
