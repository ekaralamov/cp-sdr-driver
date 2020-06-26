package app.ekaralamov.sdr.driver.permissions

import android.hardware.usb.UsbDevice
import app.ekaralamov.test.*
import com.google.common.truth.Truth.assertThat
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrowMessage
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.setMain

class GetTunerAccessViewModelSpec : DescribeSpec({
    Dispatchers.setMain(TestDispatcher)

    describe("GetTunerAccessViewModel") {
        val device = mockk<UsbDevice>()
        val getTunerAccess = mockk<GetTunerAccess>()
        val getTunerAccessPrompter =
            AnswerPrompter.ofSuspend { getTunerAccess("client package name", device) }

        val sut = GetTunerAccessViewModel(
            "client package name",
            device,
            getTunerAccess
        )

        val viewModelTestContainer = ViewModelTestContainer(sut)

        val outcomeContainer = CoroutineTestContainer.run {
            sut.outcome()
        }

        val grantPermissionToClientQuestionCollector = sut.grantPermissionToClientQuestion.test()

        describe("initially") {
            it("has no outcome") {
                outcomeContainer.hasCompleted shouldBe false
            }
            it("has no question for granting permission to the client") {
                grantPermissionToClientQuestionCollector.lastOf(1) shouldBe null
            }
            itInvokes("`getTunerAccess`", getTunerAccessPrompter, viewModelTestContainer)
        }

        describe("when `getTunerAccess` throws an error") {
            getTunerAccessPrompter.prompt(Exception("test exception")).thatsIt()

            it("throws the error as an outcome") {
                shouldThrowMessage("test exception") {
                    outcomeContainer.getResult()
                }
            }
        }

        forAll(
            row(
                GetTunerAccess.Result.DeviceAccess.Granted,
                GetTunerAccessViewModel.Outcome.Granted
            ),
            row(
                GetTunerAccess.Result.DeviceAccess.Denied,
                GetTunerAccessViewModel.Outcome.DeviceAccessDenied
            ),
            row(
                GetTunerAccess.Result.ClientPermissionDeniedPermanently,
                GetTunerAccessViewModel.Outcome.ClientPermissionDeniedPermanently
            )
        ) { getTunerAccessResult, outcome ->
            describe("when `getTunerAccess` returns $getTunerAccessResult") {
                getTunerAccessPrompter.prompt(getTunerAccessResult).thatsIt()

                it("produces $outcome outcome") {
                    outcomeContainer.getResult() shouldBe outcome
                }
            }
        }

        describe("when `getTunerAccess` returns ${GetTunerAccess.Result.GrantPermissionToClientQuestion::class.simpleName}") {

            abstract class Workaround {
                // https://github.com/mockk/mockk/issues/288
                abstract suspend fun invoke()
            }

            val workaround = mockk<Workaround>()

            forAll(
                row(null, "without"),
                row(suspend { workaround.invoke() }, "with")
            ) { useCaseNever, wo ->
                describe("$wo permanent denial option") {
                    val useCaseQuestion =
                        mockk<GetTunerAccess.Result.GrantPermissionToClientQuestion> {
                            every { never } returns useCaseNever
                        }

                    getTunerAccessPrompter.prompt(useCaseQuestion).thatsIt()

                    it("has no outcome") {
                        outcomeContainer.hasCompleted shouldBe false
                    }

                    val question by nosynchLazy {
                        grantPermissionToClientQuestionCollector.lastOf(2)!!
                    }

                    it("produces question for granting permission to the client") {
                        shouldNotThrowAny { question }
                    }

                    it("returns the same question when queried second time") {
                        val questionRequest2Collector =
                            sut.grantPermissionToClientQuestion.test()
                        questionRequest2Collector.lastOf(1) shouldBe question
                        questionRequest2Collector.close()
                    }

                    describe("when the question is answered with _yes_") {
                        val useCaseAnswerPrompter = AnswerPrompter.ofSuspend {
                            useCaseQuestion.yes()
                        }

                        question.yes()

                        it("removes the question") {
                            grantPermissionToClientQuestionCollector.lastOf(3) shouldBe null
                        }

                        itInvokes("the use case _yes_ answer", useCaseAnswerPrompter, viewModelTestContainer)

                        describe("when the use case _yes_ answer throws an error") {
                            useCaseAnswerPrompter.prompt(Exception("test exception")).thatsIt()

                            it("throws the error as an outcome") {
                                shouldThrowMessage("test exception") {
                                    outcomeContainer.getResult()
                                }
                            }
                        }

                        describe("when the use case _yes_ answer completes") {
                            forAll(
                                row(
                                    GetTunerAccess.Result.DeviceAccess.Granted,
                                    GetTunerAccessViewModel.Outcome.Granted
                                ),
                                row(
                                    GetTunerAccess.Result.DeviceAccess.Denied,
                                    GetTunerAccessViewModel.Outcome.DeviceAccessDenied
                                )
                            ) { result, outcome ->
                                describe("with $result") {
                                    useCaseAnswerPrompter.prompt(result).thatsIt()

                                    it("produces $outcome outcome") {
                                        outcomeContainer.getResult() shouldBe outcome
                                    }
                                }
                            }
                        }
                    }

                    describe("when the question is answered with _no_") {
                        val useCaseAnswerPrompter = AnswerPrompter.ofSuspend {
                            useCaseQuestion.no()
                        }

                        question.no()

                        it("removes the question") {
                            grantPermissionToClientQuestionCollector.lastOf(3) shouldBe null
                        }

                        itInvokes("the use case _no_ answer", useCaseAnswerPrompter, viewModelTestContainer)

                        describe("when the use case _no_ answer throws an error") {
                            useCaseAnswerPrompter.prompt(Exception("test exception")).thatsIt()

                            it("throws the error as an outcome") {
                                shouldThrowMessage("test exception") {
                                    outcomeContainer.getResult()
                                }
                            }
                        }

                        describe("when the use case _no_ answer completes") {
                            useCaseAnswerPrompter.prompt(Unit).thatsIt()

                            it("produces ${GetTunerAccessViewModel.Outcome.ClientPermissionDenied} outcome") {
                                outcomeContainer.getResult() shouldBe GetTunerAccessViewModel.Outcome.ClientPermissionDenied
                            }
                        }
                    }

                    if (useCaseNever != null) {
                        describe("the question") {
                            it("has permanent denial option") {
                                /* `shouldNotBe null` fails for some reason
                                 * probably related to https://github.com/mockk/mockk/issues/288
                                 */
                                assertThat(question.never).isNotNull()
                            }
                        }

                        describe("when the question is answered with _never_") {
                            val useCaseAnswerPrompter = AnswerPrompter.ofSuspend {
                                workaround.invoke()
                            }

                            question.never?.invoke()

                            it("removes the question") {
                                grantPermissionToClientQuestionCollector.lastOf(3) shouldBe null
                            }

                            itInvokes(
                                "the use case _never_ answer",
                                useCaseAnswerPrompter,
                                viewModelTestContainer
                            )

                            describe("when the use case _never_ answer throws an error") {
                                useCaseAnswerPrompter.prompt(Exception("test exception")).thatsIt()

                                it("throws the error as an outcome") {
                                    shouldThrowMessage("test exception") {
                                        outcomeContainer.getResult()
                                    }
                                }
                            }

                            describe("when the use case _never_ answer completes") {
                                useCaseAnswerPrompter.prompt(Unit).thatsIt()

                                it("produces ${GetTunerAccessViewModel.Outcome.ClientPermissionDeniedPermanently} outcome") {
                                    outcomeContainer.getResult() shouldBe GetTunerAccessViewModel.Outcome.ClientPermissionDeniedPermanently
                                }
                            }
                        }
                    } else
                        describe("the question") {
                            it("has no permanent denial option") {
                                question.never shouldBe null
                            }
                        }
                }
            }
        }

        outcomeContainer.close()
        grantPermissionToClientQuestionCollector.close()
        viewModelTestContainer.close()
    }
})
