package com.bookk.auth.domain.impl.operation.token

import com.bookk.auth.domain.api.token.entity.AuthTokens
import com.bookk.auth.domain.api.token.operation.GenerateAuthToken
import com.bookk.auth.domain.api.token.operation.GenerateAuthToken.Source
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

internal class RefreshTokenImplTest {

    private class SutFixture {
        val generateAuthToken = mockk<GenerateAuthToken>()

        val sut = RefreshTokenImpl(generateAuthToken)
    }

    @Test
    fun `should generate a new token pair from the opaque refresh token`() = runUnitTest {
        given()
        val fixture = SutFixture()
        val tokenId = Uuid.random()
        val secret = "secret"
        val token = "$tokenId.$secret"
        val tokens = AuthTokens(accessToken = "access", refreshToken = "refresh")

        coEvery { fixture.generateAuthToken(any<Source.FromRefresh>()) } returns Result.success(tokens)

        whenn()
        val result = fixture.sut.invoke(token)

        then()
        assertEquals(tokens, result.getOrThrow())
        coVerify(exactly = 1) {
            fixture.generateAuthToken(match { it is Source.FromRefresh && it.tokenId == tokenId && it.secret == secret })
        }
    }

    @Test
    fun `should fail with invalid credentials when token is malformed`() = runUnitTest {
        given()
        val fixture = SutFixture()

        whenn()
        val result = fixture.sut.invoke("not-a-valid-token")

        then()
        assertEquals(true, result.isFailure)
        coVerify(exactly = 0) { fixture.generateAuthToken(any()) }
    }
}
