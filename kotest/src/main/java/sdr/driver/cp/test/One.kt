package sdr.driver.cp.test

import io.mockk.*

inline class OneStubScope<T> constructor(internal val mockKStubScope: MockKStubScope<T, T>) {

    infix fun returns(returnValue: T) {
        mockKStubScope returns returnValue andThenThrows AssertionError("unexpected call")
    }

    infix fun throws(throwable: Throwable) {
        mockKStubScope throws throwable andThenThrows AssertionError("unexpected call")
    }

    infix fun answers(answer: MockKAnswerScope<T, T>.(Call) -> T) {
        mockKStubScope answers answer andThenThrows AssertionError("unexpected call")
    }
}

infix fun OneStubScope<Unit>.just(runs: Runs) {
     mockKStubScope just runs andThenThrows AssertionError("unexpected call")
}

fun <T> one(stubBlock: MockKMatcherScope.() -> T) = OneStubScope(every(stubBlock))

fun <T> coOne(stubBlock: suspend MockKMatcherScope.() -> T) = OneStubScope(coEvery(stubBlock))
