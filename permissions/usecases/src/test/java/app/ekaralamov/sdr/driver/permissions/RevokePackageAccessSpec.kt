package app.ekaralamov.sdr.driver.permissions

import app.ekaralamov.sdr.driver.ClientPermissionRepository
import app.ekaralamov.sdr.driver.ClientPermissionResolution
import app.ekaralamov.sdr.driver.TunerAccessToken
import app.ekaralamov.test.AnswerPrompter
import app.ekaralamov.test.CoroutineTestContainer
import app.ekaralamov.test.itCallsFor
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.mockk

class RevokePackageAccessSpec : DescribeSpec({
    describe("when `RevokePackageAccess` is invoked") {
        val permissionRepository = mockk<ClientPermissionRepository>()
        val accessTokenRegistry = mockk<TunerAccessToken.Registry<*, *>>()

        val sut = RevokePackageAccess(permissionRepository, accessTokenRegistry)

        val permissionStoringPrompter = AnswerPrompter.ofSuspend {
            permissionRepository.storeResolution("package name", ClientPermissionResolution.Permanent.Denied)
        }

        val testContainer = CoroutineTestContainer.run {
            sut("package name")
        }

        itCallsFor(
            "storing permanently denied resolution in the storage",
            permissionStoringPrompter,
            testContainer
        )

        describe("when the resolution is stored") {
            val revokeTokensPrompter = AnswerPrompter.ofSuspend {
                accessTokenRegistry.revokeTokensFor("package name")
            }

            permissionStoringPrompter.prompt(Unit).thatsIt()

            itCallsFor(
                "revoking the tokens for the package",
                revokeTokensPrompter,
                testContainer
            )

            describe("when the tokens are revoked") {
                revokeTokensPrompter.prompt(Unit).thatsIt()

                it("completes successfully") {
                    shouldNotThrowAny { testContainer.getResult() }
                }
            }
        }

        testContainer.close()
    }
})
