package sdr.driver.cp.permissions

import sdr.driver.cp.ClientPermissionRepository
import sdr.driver.cp.ClientPermissionResolution
import sdr.driver.cp.test.*
import io.kotest.assertions.throwables.shouldThrowMessage
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk

class ListAccessResolutionsSpec : DescribeSpec({
    describe("ListAccessResolutions") {
        val permissionRepository = mockk<ClientPermissionRepository>()

        val sut = ListAccessResolutions(permissionRepository, TestDispatcher)

        describe("when resolutions are collected") {

            val storageResolutionsTestFlow = TestFlow<List<Pair<String, ClientPermissionResolution>>>()
            one { permissionRepository.resolutions() } returns storageResolutionsTestFlow.flow

            val resolutionsCollector = sut().test()

            itCollects("the storage resolutions flow", storageResolutionsTestFlow, resolutionsCollector)

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
    }
})
