package sdr.driver.cp

import sdr.driver.cp.operations.shell.Database
import sdr.driver.cp.test.TestDispatcher
import sdr.driver.cp.test.test
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe

class TheClientPermissionStorageSpec : DescribeSpec({
    describe("TheClientPermissionStorage") {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        Database.Schema.create(driver)

        val sut = TheClientPermissionStorage(
            driver,
            TestDispatcher
        )

        val resolutionsCollector = sut.resolutions().test()

        it("initially emits empty resolutions list") {
            resolutionsCollector.lastOf(1) shouldBe emptyList()
        }

        it("returns `null` resolution for an unknown package") {
            sut.retrieveResolutionFor("unknown package") shouldBe null
        }

        forAll(
            row(ClientPermissionResolution.Denied, ClientPermissionResolution.Permanent.Granted),
            row(ClientPermissionResolution.Permanent.Denied, ClientPermissionResolution.Permanent.Granted),
            row(ClientPermissionResolution.Permanent.Granted, ClientPermissionResolution.Permanent.Denied)
        ) { resolution1, resolution2 ->
            describe("when $resolution1 is stored for a package") {
                sut.storeResolution("package name", resolution1)

                it("emits updated resolutions list") {
                    resolutionsCollector.lastOf(2) shouldBe listOf("package name" to resolution1)
                }

                it("returns the resolution on retrieval") {
                    sut.retrieveResolutionFor("package name") shouldBe resolution1
                }

                describe("when new resolution for the same package is stored") {
                    sut.storeResolution("package name", resolution2)

                    it("emits updated resolutions list") {
                        resolutionsCollector.lastOf(3) shouldBe listOf("package name" to resolution2)
                    }
                }

                describe("when resolution for another package is stored") {
                    sut.storeResolution("another package name", resolution2)

                    it("emits updated resolutions list") {
                        with(resolutionsCollector.lastOf(3)) {
                            size shouldBe 2
                            contains("package name" to resolution1) shouldBe true
                            contains("another package name" to resolution2) shouldBe true
                        }
                    }
                }

                describe("when the resolution is deleted") {
                    sut.deleteResolutionFor("package name")

                    it("emits updated resolutions list") {
                        resolutionsCollector.lastOf(3) shouldBe emptyList()
                    }
                }
            }
        }

        resolutionsCollector.close()
    }
})
