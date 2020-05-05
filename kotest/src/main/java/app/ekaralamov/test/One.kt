package app.ekaralamov.test

import io.mockk.MockKMatcherScope
import io.mockk.MockKStubScope
import io.mockk.every

inline class OneStubScope<T> constructor(private val mockKStubScope: MockKStubScope<T, T>) {

    infix fun returns(returnValue: T) {
        mockKStubScope returns returnValue andThenThrows AssertionError("unexpected call")
    }
}

fun <T> one(stubBlock: MockKMatcherScope.() -> T) = OneStubScope(every(stubBlock))
