package app.ekaralamov.test

import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.DescribeSpecDsl
import io.kotest.matchers.shouldBe

suspend fun DescribeSpecDsl.DescribeScope.itCallsFor(
    callDesc: String,
    prompter: AnswerPrompter<*>,
    testContainer: CoroutineTestContainer<*>
) {
    it("calls for $callDesc") {
        prompter.lastCallState shouldBe AnswerPrompter.CallState.Waiting
    }

    describe("when cancelled while waiting for the outstanding call to complete") {
        testContainer.cancel()

        it("cancels the outstanding call") {
            prompter.lastCallState shouldBe AnswerPrompter.CallState.Cancelled
        }
    }

    describe("when the outstanding call throws") {
        prompter.prompt(Exception("test exception"))

        it("throws") {
            shouldThrowAny { testContainer.getResult() }
        }
    }
}
