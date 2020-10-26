package sdr.driver.cp.opening

import android.hardware.usb.UsbDevice
import android.os.ParcelFileDescriptor
import sdr.driver.cp.PermissionRevokedException
import sdr.driver.cp.TunerAccessToken
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Named
import kotlin.concurrent.withLock

class TunerSession @AssistedInject constructor(
    @Assisted private val nativeSession: NativeTunerSession,
    @Named("IO") private val pumpDispatcher: CoroutineDispatcher,
    @Named("default") private val releaseTokenDispatcher: CoroutineDispatcher
) : TunerAccessToken.Session {

    @AssistedInject.Factory
    interface Factory {

        fun create(nativeSession: NativeTunerSession): TunerSession
    }

    private val lock = ReentrantLock()

    private var commandsPipe: Pipe? = null
    private var commandsPumping: Job? = null
    private val commandsClosed = lock.newCondition()

    private var dataPipe: Pipe? = null
    private var dataPumping: Job? = null
    private val dataClosed = lock.newCondition()

    private var closing = false

    override suspend fun close() {
        lock.withLock {
            if (closing)
                throw AssertionError("closing should be managed by TunerAccessToken and should happen only once")
            closing = true
        }

        nativeSession.stopPumps()

        commandsPumping?.join()
        dataPumping?.join()

        nativeSession.close()
    }

    fun startCommandsPump(accessToken: TunerAccessToken<UsbDevice, TunerSession>): ParcelFileDescriptor =
        lock.withLock {
            try {
                if (closing) throw PermissionRevokedException()
                while (commandsPipe != null) commandsClosed.await()
                if (closing) {
                    commandsClosed.signal()
                    throw PermissionRevokedException()
                }
                val pipe = Pipe()
                commandsPipe = pipe
                commandsPumping = GlobalScope.launch(pumpDispatcher) {
                    try {
                        nativeSession.pumpCommands(inputFD = commandsPipe!!.input.fd)
                    } finally {
                        release(accessToken)

                        commandsPipe!!.close()
                        lock.withLock {
                            commandsPipe = null
                            commandsClosed.signal()
                        }
                    }
                }
                pipe.output
            } catch (throwable: Throwable) {
                release(accessToken)
                throw throwable
            }
        }

    fun startDataPump(accessToken: TunerAccessToken<UsbDevice, TunerSession>): ParcelFileDescriptor =
        lock.withLock {
            try {
                if (closing) throw PermissionRevokedException()
                while (dataPipe != null) dataClosed.await()
                if (closing) {
                    dataClosed.signal()
                    throw PermissionRevokedException()
                }
                val pipe = Pipe()
                dataPipe = pipe
                dataPumping = GlobalScope.launch(pumpDispatcher) {
                    try {
                        nativeSession.pumpData(dataPipe!!.output.fd)
                    } finally {
                        release(accessToken)

                        dataPipe!!.close()
                        lock.withLock {
                            dataPipe = null
                            dataClosed.signal()
                        }
                    }
                }
                pipe.input
            } catch (throwable: Throwable) {
                release(accessToken)
                throw throwable
            }
        }

    private fun release(accessToken: TunerAccessToken<UsbDevice, TunerSession>) {
        GlobalScope.launch(releaseTokenDispatcher) { accessToken.release() }
    }
}

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
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
