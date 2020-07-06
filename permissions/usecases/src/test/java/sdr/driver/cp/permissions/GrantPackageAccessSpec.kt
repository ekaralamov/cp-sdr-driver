package sdr.driver.cp.permissions

import sdr.driver.cp.ClientPermissionRepository
import sdr.driver.cp.ClientPermissionResolution
import sdr.driver.cp.test.AnswerPrompter
import sdr.driver.cp.test.CoroutineTestContainer
import sdr.driver.cp.test.itCallsFor
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.mockk

class GrantPackageAccessSpec : DescribeSpec({
    describe("when `GrantPackageAccess` is invoked") {
        val permissionRepository = mockk<ClientPermissionRepository>()

        val sut = GrantPackageAccess(permissionRepository)

        val permissionStoringPrompter = AnswerPrompter.ofSuspend {
            permissionRepository.storeResolution("package name", ClientPermissionResolution.Permanent.Granted)
        }

        val testContainer = CoroutineTestContainer.run {
            sut("package name")
        }

        itCallsFor(
            "storing granted resolution in the storage",
            permissionStoringPrompter,
            testContainer
        )

        describe("when the resolution is stored") {
            permissionStoringPrompter.prompt(Unit).thatsIt()

            it("completes successfully") {
                shouldNotThrowAny { testContainer.getResult() }
            }
        }

        testContainer.close()
    }
})
