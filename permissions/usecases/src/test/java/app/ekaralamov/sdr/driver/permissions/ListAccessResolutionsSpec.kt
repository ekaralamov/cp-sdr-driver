package app.ekaralamov.sdr.driver.permissions

import app.ekaralamov.sdr.driver.ClientPermissionRepository
import app.ekaralamov.sdr.driver.ClientPermissionResolution
import app.ekaralamov.test.TestDispatcher
import app.ekaralamov.test.TestFlow
import app.ekaralamov.test.test
import io.kotest.assertions.throwables.shouldThrowMessage
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class ListAccessResolutionsSpec : DescribeSpec({
    describe("when `ListAccessResolutions` is invoked") {
        val permissionRepository = mockk<ClientPermissionRepository>()

        val sut = ListAccessResolutions(permissionRepository, TestDispatcher)

        val storageResolutionsTestFlow = TestFlow<List<Pair<String, ClientPermissionResolution>>>()
        every { permissionRepository.resolutions() } returns storageResolutionsTestFlow.flow

        val resolutionsCollector = sut().test()

        it("starts the storage resolutions flow") {
            storageResolutionsTestFlow.wasStarted shouldBe true
        }

        describe("when the storage emits a list with granted, denied and permanently denied resolutions") {
            storageResolutionsTestFlow.emit(
                listOf(
                    "package1" to ClientPermissionResolution.Permanent.Granted,
                    "package2" to ClientPermissionResolution.Denied,
                    "package3" to ClientPermissionResolution.Permanent.Denied
                )
            )

            it("emits a list of (true, false, false)") {
                resolutionsCollector.lastOf(1) shouldBe listOf(
                    "package1" to true,
                    "package2" to false,
                    "package3" to false
                )
            }

            describe("when the storage emits second value—empty list") {
                storageResolutionsTestFlow.emit(emptyList())

                it("emits empty list") {
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
})
