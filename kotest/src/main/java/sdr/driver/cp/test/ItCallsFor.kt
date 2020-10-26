package sdr.driver.cp.test

import io.kotest.assertions.throwables.shouldThrowMessage
import io.kotest.core.spec.style.scopes.DescribeScope
import io.kotest.matchers.shouldBe

suspend fun DescribeScope.itCallsFor(
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

suspend fun DescribeScope.itInvokes(
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

suspend fun DescribeScope.itCollects(
    flowDesc: String,
    testFlow: TestFlow<*>,
    testContainer: ViewModelTestContainer
) {
    it("starts $flowDesc") {
        testFlow.state shouldBe TestFlow.State.Started
    }

    describe("when cleared") {
        testContainer.clear()

        it("cancels $flowDesc collection") {
            testFlow.state shouldBe TestFlow.State.Cancelled
        }
    }
}

suspend fun DescribeScope.itCollects(
    flowDesc: String,
    testFlow: TestFlow<*>,
    testCollector: FlowTestCollector<*>
) {
    it("starts $flowDesc") {
        testFlow.state shouldBe TestFlow.State.Started
    }

    describe("when collection is cancelled") {
        testCollector.close()

        it("cancels $flowDesc collection") {
            testFlow.state shouldBe TestFlow.State.Cancelled
        }
    }
}
