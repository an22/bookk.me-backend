package com.bookk.core.test

internal class TestContext(
    var givenCalled: Boolean = false,
    var whenCalled: Boolean = false,
    var thenCalled: Boolean = false
)

internal object TestHolder {
    internal var context: TestContext? = null

    fun givenCalled() {
        context?.givenCalled = true
    }

    fun whenCalled() {
        context?.whenCalled = true
    }

    fun thenCalled() {
        context?.thenCalled = true
    }

    fun assertFormat() {
        assert(context?.givenCalled == true) { "Given step is missing" }
        assert(context?.whenCalled == true) { "When step is missing" }
        assert(context?.thenCalled == true) { "Then step is missing" }
    }
}

fun runTest(body: () -> Unit) {
    TestHolder.context = TestContext()
    body()
    TestHolder.assertFormat()
    TestHolder.context = null
}

fun given() {
    TestHolder.givenCalled()
}

fun whenn() {
    TestHolder.whenCalled()
}

fun then() {
    TestHolder.thenCalled()
}