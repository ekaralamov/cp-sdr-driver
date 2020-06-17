package app.ekaralamov.test

import io.kotest.assertions.throwables.shouldThrowMessage
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

        it("passes the throwable through") {
            shouldThrowMessage("test exception") { testContainer.getResult() }
        }
    }
}

suspend fun DescribeSpecDsl.DescribeScope.itInvokes(
    callDesc: String,
    prompter: AnswerPrompter<*>,
    testContainer: ViewModelTestContainer
) {
    it("invokes $callDesc") {
        prompter.lastCallState shouldBe AnswerPrompter.CallState.Waiting
    }

    describe("when cleared while waiting for $callDesc to complete") {
        testContainer.clear()

        it("cancels the $callDesc invocation") {
            prompter.lastCallState shouldBe AnswerPrompter.CallState.Cancelled
        }
    }
}
