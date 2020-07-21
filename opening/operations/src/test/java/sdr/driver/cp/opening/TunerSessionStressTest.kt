package sdr.driver.cp.opening

import android.hardware.usb.UsbDevice
import android.os.ParcelFileDescriptor
import sdr.driver.cp.PermissionRevokedException
import sdr.driver.cp.TunerAccessToken
import sdr.driver.cp.test.FailOnThreadExceptionRule
import sdr.driver.cp.test.just
import sdr.driver.cp.test.one
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class TunerSessionStressTest {

    @get:Rule
    val failOnThreadExceptionRule = FailOnThreadExceptionRule()

    private lateinit var nativeSession: NativeTunerSession

    private lateinit var sut: TunerSession

    @Before
    fun setUp() {
        nativeSession = mockk()

        val mockPfd = mockk<ParcelFileDescriptor> {
            every { fd } returns 0xFD
            every { close() } just Runs
        }
        mockkStatic(ParcelFileDescriptor::class)
        every { ParcelFileDescriptor.createPipe() } returns arrayOf(mockPfd, mockPfd)

        sut = TunerSession(
            nativeSession = nativeSession,
            pumpDispatcher = Dispatchers.IO,
            releaseTokenDispatcher = Dispatchers.Unconfined
        )
    }

    @After
    fun tearDown() {
        unmockkStatic(ParcelFileDescriptor::class)
    }

    @Test
    fun test() {
        val requestCount = 150
        val closeAfter = 140

        val token = mockk<TunerAccessToken<UsbDevice, TunerSession>> {
            coEvery { release() } just Runs
        }
        one { nativeSession.stopPumps() } just Runs
        one { nativeSession.close() } just Runs

        val commandsPumping = AtomicBoolean()
        val dataPumping = AtomicBoolean()
        val pumpingsCount = AtomicInteger()
        every { nativeSession.pumpCommands(0xFD) } answers {
            assertThat(commandsPumping.getAndSet(true)).isFalse()
            if (pumpingsCount.incrementAndGet() == closeAfter)
                GlobalScope.launch { sut.close() }
            Thread.yield()
            commandsPumping.set(false)
        }
        every { nativeSession.pumpData(0xFD) } answers {
            assertThat(dataPumping.getAndSet(true)).isFalse()
            if (pumpingsCount.incrementAndGet() == closeAfter)
                GlobalScope.launch { sut.close() }
            Thread.yield()
            dataPumping.set(false)
        }

        val rejected = AtomicInteger()
        val commandsRequest = Runnable {
            try {
                sut.startCommandsPump(token)
            } catch (expected: PermissionRevokedException) {
                rejected.incrementAndGet()
            }
        }
        val dataRequest = Runnable {
            try {
                sut.startDataPump(token)
            } catch (expected: PermissionRevokedException) {
                rejected.incrementAndGet()
            }
        }

        val random = ThreadLocalRandom.current()
        val requestThreads = Array(requestCount) {
            Thread(
                when (random.nextInt(2)) {
                    0 -> commandsRequest
                    1 -> dataRequest
                    else -> throw AssertionError()
                }
            ).apply { start() }
        }

        requestThreads.forEach { it.join() }
        Thread.sleep(10)

        println("rejected: ${rejected.get()}")
        assertThat(rejected.get()).isIn(1..(requestCount-closeAfter))
    }
}
