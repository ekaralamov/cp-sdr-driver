package sdr.driver.cp.opening

import android.hardware.usb.UsbDevice
import android.os.ParcelFileDescriptor
import sdr.driver.cp.PermissionRevokedException
import sdr.driver.cp.TunerAccessToken
import sdr.driver.cp.test.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.core.spec.style.DescribeSpecDsl
import io.kotest.matchers.shouldBe
import io.mockk.*
import kotlinx.coroutines.*
import java.util.concurrent.CountDownLatch
import kotlin.coroutines.coroutineContext

@Suppress("BlockingMethodInNonBlockingContext")
class TunerSessionSpec : DescribeSpec({

    describe("TunerSession") {
        val nativeSession = mockk<NativeTunerSession>()

        mockkStatic(ParcelFileDescriptor::class)
        val pipe = MockPipe()
        one { ParcelFileDescriptor.createPipe() } returns pipe.descriptors

        describe("when a commands pumping request is made") {
            val token = mockk<TunerAccessToken<UsbDevice, TunerSession>>()

            context("immediate pumping dispatch and end") {
                val sut = TunerSession(
                    nativeSession = nativeSession,
                    pumpDispatcher = TestDispatcher,
                    releaseTokenDispatcher = TestDispatcher
                )

                one { nativeSession.pumpCommands(pipe.inputFD) } just Runs
                val releasePrompter = AnswerPrompter.ofSuspend{ token.release() }

                val fd = sut.startCommandsPump(token)

                it("returns the output file descriptor") {
                    fd shouldBe pipe.output
                }

                it("calls the native pump") {
                    verify { nativeSession.pumpCommands(pipe.inputFD) }
                }

                itCloses("the", pipe)

                it("calls for releasing the token once") {
                    releasePrompter.prompt(Unit).thatsIt()
                }
            }

            context("pumping does not end immediately") {
                val sut = TunerSession(
                    nativeSession = nativeSession,
                    pumpDispatcher = Dispatchers.IO,
                    releaseTokenDispatcher = TestDispatcher
                )

                val latch = CountDownLatch(1)
                one { nativeSession.pumpCommands(pipe.inputFD) } answers { latch.await() }

                val fd = sut.startCommandsPump(token)

                it("returns the output file descriptor") {
                    fd shouldBe pipe.output
                }

                itDoesNotClose("the", pipe)

                describe("when second commands pumping request is made") {
                    val token2 = mockk<TunerAccessToken<UsbDevice, TunerSession>>()

                    val request2Container = CoroutineTestContainer.run(Dispatchers.IO) {
                        sut.startCommandsPump(token2)
                    }
                    delay(10)

                    describe("when pumping for first request ends") {
                        val releasePrompter = AnswerPrompter.ofSuspend { token.release() }

                        val pipe2 = MockPipe()
                        one { ParcelFileDescriptor.createPipe() } returns pipe2.descriptors
                        one { nativeSession.pumpCommands(pipe2.inputFD) } just Runs

                        val releasePrompter2 = AnswerPrompter.ofSuspend { token2.release() }

                        latch.countDown()
                        delay(10)

                        it("returns the output file descriptor for the second request") {
                            request2Container.getResult() shouldBe pipe2.output
                        }

                        it("calls the native pump for the second request") {
                            verify { nativeSession.pumpCommands(pipe2.inputFD) }
                        }

                        itCloses("first request", pipe)
                        itCloses("second request", pipe2)

                        it("calls for releasing first request token once") {
                            releasePrompter.prompt(Unit).thatsIt()
                        }
                        it("calls for releasing second request token once") {
                            releasePrompter2.prompt(Unit).thatsIt()
                        }
                    }

                    describe("when third commands pumping request is made") {
                        val token3 = mockk<TunerAccessToken<UsbDevice, TunerSession>>()

                        val request3Container = CoroutineTestContainer.run(Dispatchers.IO) {
                            sut.startCommandsPump(token3)
                        }
                        delay(10)

                        describe("when pumping for first request ends") {
                            coOne { token.release() } just Runs

                            val pipe2 = MockPipe()
                            one { ParcelFileDescriptor.createPipe() } returns pipe2.descriptors
                            val latch2 = CountDownLatch(1)
                            one { nativeSession.pumpCommands(pipe2.inputFD) } answers { latch2.await() }

                            latch.countDown()
                            delay(10)

                            describe("when pumping for the second served request ends") {
                                coOne { token2.release() } just Runs
                                coOne { token3.release() } just Runs

                                val pipe3 = MockPipe()
                                one { ParcelFileDescriptor.createPipe() } returns pipe3.descriptors
                                one { nativeSession.pumpCommands(pipe3.inputFD) } just Runs

                                latch2.countDown()
                                delay(10)

                                it("pumps for the third served request") {
                                    verify { nativeSession.pumpCommands(pipe3.inputFD) }
                                }
                            }
                        }

                        describe("when closed") {
                            every { nativeSession.stopPumps() } just Runs

                            GlobalScope.launch(coroutineContext + Dispatchers.IO) { sut.close() }
                            delay(10)

                            describe("when pumping for first request ends") {
                                coOne { token.release() } just Runs
                                coOne { token2.release() } just Runs
                                coOne { token3.release() } just Runs
                                one { nativeSession.close() } just Runs

                                latch.countDown()
                                delay(10)

                                it("calls for stopping pumps") {
                                    verify { nativeSession.stopPumps() }
                                }

                                itCloses("first request", pipe)

                                it("signals revoked permission for the second request") {
                                    shouldThrow<PermissionRevokedException> { request2Container.getResult() }
                                }
                                it("signals revoked permission for the third request") {
                                    shouldThrow<PermissionRevokedException> { request3Container.getResult() }
                                }

                                it("closes the native session") {
                                    verify { nativeSession.close() }
                                }
                            }
                        }
                        request3Container.close()
                    }
                    request2Container.close()
                }

                describe("when a data pumping request is made") {
                    val dataToken = mockk<TunerAccessToken<UsbDevice, TunerSession>> {
                        coOne { release() } just Runs
                    }

                    val dataPipe = MockPipe()
                    one { ParcelFileDescriptor.createPipe() } returns dataPipe.descriptors
                    one { nativeSession.pumpData(dataPipe.outputFD) } just Runs

                    val dataFD = sut.startDataPump(dataToken)

                    it("serves it alongside the commands one") {
                        dataFD shouldBe dataPipe.input
                    }

                    coOne { token.release() } just Runs
                    latch.countDown()
                    delay(10)
                }
            }
        }

        describe("when a data pumping request is made") {
            val token = mockk<TunerAccessToken<UsbDevice, TunerSession>>()

            context("immediate pumping dispatch and end") {
                val sut = TunerSession(
                    nativeSession = nativeSession,
                    pumpDispatcher = TestDispatcher,
                    releaseTokenDispatcher = TestDispatcher
                )

                one { nativeSession.pumpData(pipe.outputFD) } just Runs
                val releasePrompter = AnswerPrompter.ofSuspend { token.release() }

                val fd = sut.startDataPump(token)

                it("returns the input file descriptor") {
                    fd shouldBe pipe.input
                }

                it("calls the native pump") {
                    verify { nativeSession.pumpData(pipe.outputFD) }
                }

                itCloses("the", pipe)

                it("calls for releasing the token once") {
                    releasePrompter.prompt(Unit).thatsIt()
                }
            }

            context("pumping does not end immediately") {
                val sut = TunerSession(
                    nativeSession = nativeSession,
                    pumpDispatcher = Dispatchers.IO,
                    releaseTokenDispatcher = TestDispatcher
                )

                val latch = CountDownLatch(1)
                one { nativeSession.pumpData(pipe.outputFD) } answers { latch.await() }

                val fd = sut.startDataPump(token)

                it("returns the input file descriptor") {
                    fd shouldBe pipe.input
                }

                itDoesNotClose("the", pipe)

                describe("when second data pumping request is made") {
                    val token2 = mockk<TunerAccessToken<UsbDevice, TunerSession>>()

                    val request2Container = CoroutineTestContainer.run(Dispatchers.IO) {
                        sut.startDataPump(token2)
                    }
                    delay(10)

                    describe("when pumping for first request ends") {
                        val releasePrompter = AnswerPrompter.ofSuspend { token.release() }

                        val pipe2 = MockPipe()
                        one { ParcelFileDescriptor.createPipe() } returns pipe2.descriptors
                        one { nativeSession.pumpData(pipe2.outputFD) } just Runs

                        val releasePrompter2 = AnswerPrompter.ofSuspend { token2.release() }

                        latch.countDown()
                        delay(10)

                        it("returns the input file descriptor for the second request") {
                            request2Container.getResult() shouldBe pipe2.input
                        }

                        it("calls the native pump for the second request") {
                            verify { nativeSession.pumpData(pipe2.outputFD) }
                        }

                        itCloses("first request", pipe)
                        itCloses("second request", pipe2)

                        it("calls for releasing first request token once") {
                            releasePrompter.prompt(Unit).thatsIt()
                        }
                        it("calls for releasing second request token once") {
                            releasePrompter2.prompt(Unit).thatsIt()
                        }
                    }

                    describe("when third data pumping request is made") {
                        val token3 = mockk<TunerAccessToken<UsbDevice, TunerSession>>()

                        val request3Container = CoroutineTestContainer.run(Dispatchers.IO) {
                            sut.startDataPump(token3)
                        }
                        delay(10)

                        describe("when pumping for first request ends") {
                            coOne { token.release() } just Runs

                            val pipe2 = MockPipe()
                            one { ParcelFileDescriptor.createPipe() } returns pipe2.descriptors
                            val latch2 = CountDownLatch(1)
                            one { nativeSession.pumpData(pipe2.outputFD) } answers { latch2.await() }

                            latch.countDown()
                            delay(10)

                            describe("when pumping for the second served request ends") {
                                coOne { token2.release() } just Runs
                                coOne { token3.release() } just Runs

                                val pipe3 = MockPipe()
                                one { ParcelFileDescriptor.createPipe() } returns pipe3.descriptors
                                one { nativeSession.pumpData(pipe3.outputFD) } just Runs

                                latch2.countDown()
                                delay(10)

                                it("pumps for the third served request") {
                                    verify { nativeSession.pumpData(pipe3.outputFD) }
                                }
                            }
                        }

                        describe("when closed") {
                            every { nativeSession.stopPumps() } just Runs

                            GlobalScope.launch(coroutineContext + Dispatchers.IO) { sut.close() }
                            delay(10)

                            describe("when pumping for first request ends") {
                                coOne { token.release() } just Runs
                                coOne { token2.release() } just Runs
                                coOne { token3.release() } just Runs
                                one { nativeSession.close() } just Runs

                                latch.countDown()
                                delay(10)

                                it("calls for stopping pumps") {
                                    verify { nativeSession.stopPumps() }
                                }

                                itCloses("first request", pipe)

                                it("signals revoked permission for the second request") {
                                    shouldThrow<PermissionRevokedException> { request2Container.getResult() }
                                }
                                it("signals revoked permission for the third request") {
                                    shouldThrow<PermissionRevokedException> { request3Container.getResult() }
                                }

                                it("closes the native session") {
                                    verify { nativeSession.close() }
                                }
                            }
                        }
                        request3Container.close()
                    }
                    request2Container.close()
                }
            }
        }

        describe("when closed") {
            every { nativeSession.stopPumps() } just Runs
            one { nativeSession.close() } just Runs

            val sut = TunerSession(
                nativeSession = nativeSession,
                pumpDispatcher = TestDispatcher,
                releaseTokenDispatcher = TestDispatcher
            )

            sut.close()

            it("closes the native session") {
                verify { nativeSession.close() }
            }
        }

        unmockkStatic(ParcelFileDescriptor::class)
    }
}) {

    private val failOnThreadExceptionListener = FailOnThreadExceptionListener()

    override fun listeners() = listOf(failOnThreadExceptionListener)
}

private class MockPipe {

    val descriptors: Array<ParcelFileDescriptor> = arrayOf(mockPfd(), mockPfd())

    val input = descriptors[0]
    val output = descriptors[1]

    val inputFD = input.fd
    val outputFD = output.fd

    companion object {

        private var nextFd = 1

        private fun mockPfd() = mockk<ParcelFileDescriptor> {
            every { fd } returns nextFd++
            every { close() } just Runs
        }
    }
}

private suspend fun DescribeSpecDsl.DescribeScope.itCloses(pipeName: String, pipe: MockPipe) =
    it("closes $pipeName pipe") {
        verify {
            pipe.input.close()
            pipe.output.close()
        }
    }

private suspend fun DescribeSpecDsl.DescribeScope.itDoesNotClose(pipeName: String, pipe: MockPipe) =
    it("does not close $pipeName pipe") {
        verify(exactly = 0) {
            pipe.input.close()
            pipe.output.close()
        }
    }
