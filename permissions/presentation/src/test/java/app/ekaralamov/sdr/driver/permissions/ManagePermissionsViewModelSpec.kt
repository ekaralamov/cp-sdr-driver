package app.ekaralamov.sdr.driver.permissions

import app.ekaralamov.test.*
import io.kotest.assertions.throwables.shouldThrowMessage
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.setMain

class ManagePermissionsViewModelSpec : DescribeSpec({
    Dispatchers.setMain(TestDispatcher)

    describe("ManagePermissionsViewModel") {
        val listAccessResolutions = mockk<ListAccessResolutions>()
        val accessResolutionsTestFlow = TestFlow<List<Pair<String, Boolean>>>()
        one { listAccessResolutions() } returns accessResolutionsTestFlow.flow

        val revokePackageAccess = mockk<RevokePackageAccess>()
        val grantPackageAccess = mockk<GrantPackageAccess>()

        val sut = ManagePermissionsViewModel(listAccessResolutions, revokePackageAccess, grantPackageAccess)

        val viewModelTestContainer = ViewModelTestContainer(sut)

        describe("when access resolutions are requested") {
            val resolutionsConsumer = sut.accessResolutions.test()

            it("initially emits `null`") {
                resolutionsConsumer.lastOf(1) shouldBe null
            }

            itCollects("the access resolutions flow", accessResolutionsTestFlow, viewModelTestContainer)

            describe("when the access resolutions flow emits") {
                val accessResolutionsList = listOf(
                    "package 1" to true,
                    "package 2" to false
                )
                accessResolutionsTestFlow.emit(accessResolutionsList)

                it("emits the access resolutions") {
                    checkNotNull(resolutionsConsumer.lastOf(2)).getOrThrow() shouldBe accessResolutionsList
                }

                describe("when the access resolutions flow emits second time") {
                    accessResolutionsTestFlow.emit(emptyList())

                    it("emits the second access resolutions") {
                        checkNotNull(resolutionsConsumer.lastOf(3)).getOrThrow() shouldBe emptyList()
                    }
                }
            }

            describe("when the access resolutions flow throws") {
                accessResolutionsTestFlow.finishWith(Exception("test exception"))

                it("emits the throwable") {
                    checkNotNull(resolutionsConsumer.lastOf(2)).exceptionOrNull()!!.message shouldBe "test exception"
                }
            }

            resolutionsConsumer.close()
        }

        describe("when access is revoked") {
            context("the revoke access use case completes successfully") {
                coOne { revokePackageAccess(any()) } just Runs

                sut.revokeAccess("package name")

                it("calls the revoke access use case") {
                    coVerify { revokePackageAccess("package name") }
                }
            }

            context("the revoke access use case throws") {
                coOne { revokePackageAccess("package name") } throws Exception("test exception")

                it("passes the throwable through") {
                    shouldThrowMessage("test exception") {
                        sut.revokeAccess("package name")
                    }
                }
            }
        }

        describe("when access is granted") {
            context("the grant access use case completes successfully") {
                coOne { grantPackageAccess(any()) } just Runs

                sut.grantAccess("package name")

                it("calls the grant access use case") {
                    coVerify { grantPackageAccess("package name") }
                }
            }

            context("the grant access use case throws") {
                coOne { grantPackageAccess("package name") } throws Exception("test exception")

                it("passes the throwable through") {
                    shouldThrowMessage("test exception") {
                        sut.grantAccess("package name")
                    }
                }
            }
        }

        viewModelTestContainer.close()
    }
})
