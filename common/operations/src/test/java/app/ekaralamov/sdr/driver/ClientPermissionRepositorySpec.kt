package app.ekaralamov.sdr.driver

import app.ekaralamov.test.*
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrowMessage
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class ClientPermissionRepositorySpec : DescribeSpec({
    describe("ClientPermissionRepository") {
        val storage = mockk<ClientPermissionStorage>()

        val sut = ClientPermissionRepository(storage)

        describe("when retrieving a resolution") {
            val prompter = AnswerPrompter.ofSuspend {
                storage.retrieveResolutionFor("package name")
            }
            val testContainer = CoroutineTestContainer.run {
                sut.retrieveResolutionFor("package name")
            }

            itCallsFor(
                "retrieving the resolution from the storage",
                prompter,
                testContainer
            )

            forAll(
                row(ClientPermissionResolution.Denied),
                row(ClientPermissionResolution.Permanent.Denied),
                row(ClientPermissionResolution.Permanent.Granted),
                row(null)
            ) { resolution ->
                describe("when the storage returns $resolution") {
                    prompter.prompt(resolution).thatsIt()

                    it("returns $resolution") {
                        testContainer.getResult() shouldBe resolution
                    }
                }
            }

            testContainer.close()
        }

        forAll(
            row(ClientPermissionResolution.Denied),
            row(ClientPermissionResolution.Permanent.Denied),
            row(ClientPermissionResolution.Permanent.Granted)
        ) { resolution ->
            describe("when storing $resolution resolution") {
                val prompter = AnswerPrompter.ofSuspend {
                    storage.storeResolution("package name", resolution)
                }
                val testContainer = CoroutineTestContainer.run {
                    sut.storeResolution("package name", resolution)
                }

                itCallsFor(
                    "storing the resolution in the storage",
                    prompter,
                    testContainer
                )

                describe("when the storage succeeds") {
                    prompter.prompt(Unit).thatsIt()

                    it("succeeds") {
                        shouldNotThrowAny { testContainer.getResult() }
                    }
                }

                testContainer.close()
            }
        }

        describe("when deleting resolution") {
            val prompter = AnswerPrompter.ofSuspend {
                storage.deleteResolutionFor("package name")
            }
            val testContainer = CoroutineTestContainer.run {
                sut.deleteResolutionFor("package name")
            }

            itCallsFor(
                "deleting the resolution from the storage",
                prompter,
                testContainer
            )

            describe("when the storage succeeds") {
                prompter.prompt(Unit).thatsIt()

                it("succeeds") {
                    shouldNotThrowAny { testContainer.getResult() }
                }
            }

            testContainer.close()
        }

        describe("when resolutions flow is started") {
            val storageResolutionsTestFlow = TestFlow<List<Pair<String, ClientPermissionResolution>>>()
            every { storage.resolutions() } returns storageResolutionsTestFlow.flow

            val resolutionsCollector = sut.resolutions().test()

            itCollects("the storage resolutions flow", storageResolutionsTestFlow, resolutionsCollector)

            describe("when the storage emits a value") {
                val value = listOf("package name" to ClientPermissionResolution.Permanent.Granted)
                storageResolutionsTestFlow.emit(value)

                it("emits the value") {
                    resolutionsCollector.lastOf(1) shouldBe value
                }

                describe("when the storage emits second value") {
                    storageResolutionsTestFlow.emit(emptyList())

                    it("emits the second value") {
                        resolutionsCollector.lastOf(2) shouldBe emptyList()
                    }
                }
            }

            describe("when the storage throws") {
                storageResolutionsTestFlow.finishWith(Exception("test exception"))

                it("passes the throwable through") {
                    shouldThrowMessage("test exception") {
                        resolutionsCollector.lastOf(1)
                    }
                }
            }

            resolutionsCollector.close()
        }
    }
})
