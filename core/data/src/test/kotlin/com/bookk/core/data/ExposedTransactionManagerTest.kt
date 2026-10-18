package com.bookk.core.data

import com.bookk.core.data.test.createTestDatabase
import com.bookk.core.domain.entity.Error
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.coroutines.cancellation.CancellationException

internal class ExposedTransactionManagerTest {

    private class SutFixture {
        val sut = ExposedTransactionManager(createTestDatabase())
    }

    @Test
    fun `should return success holding the transaction value`() = runUnitTest {
        given()
        val fixture = SutFixture()

        whenn()
        val result = fixture.sut.transaction { "value" }

        then()
        assertEquals("value", result.getOrNull())
    }

    @Test
    fun `should map an unexpected exception to an unknown error`() = runUnitTest {
        given()
        val fixture = SutFixture()

        whenn()
        val result = fixture.sut.transaction { throw IllegalStateException("failure") }

        then()
        assertTrue(result.exceptionOrNull() is Error.UnknownError)
    }

    @Test
    fun `should rethrow cancellation exception instead of returning failure`() = runUnitTest {
        given()
        val fixture = SutFixture()

        whenn()
        val thrown = runCatching { fixture.sut.transaction { throw CancellationException("cancelled") } }.exceptionOrNull()

        then()
        assertTrue(thrown is CancellationException)
        assertEquals("cancelled", thrown?.message)
    }
}
