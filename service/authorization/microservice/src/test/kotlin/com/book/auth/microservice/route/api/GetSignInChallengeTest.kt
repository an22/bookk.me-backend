package com.book.auth.microservice.route.api

import com.book.auth.domain.api.authentication.entity.AssertionStartResponse
import com.book.auth.domain.api.authentication.operation.StartAssertion
import com.book.auth.microservice.route.AuthRouting
import com.bookk.core.service.test.createTestClient
import com.bookk.core.service.test.routeTest
import com.bookk.core.service.test.setupApplication
import com.bookk.core.test.given
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.get
import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.koin.dsl.module
import java.util.UUID
import kotlin.test.Test

internal class GetSignInChallengeTest {

    @Test
    fun verifyAnswerOnSuccess() = routeTest {
        given()
        val useCase: StartAssertion = mockk()
        setupApplication(
            diModule = module {
                single { useCase }
            },
            routeUnderTest = { getVerificationChallenge() }
        )
        val client = createTestClient()
        val answer = AssertionStartResponse(UUID.randomUUID().toString(), "mock")
        coEvery { useCase.invoke() } returns Result.success(answer)

        whenn()
        val response = client.get(AuthRouting.Api.Auth.SignIn.PassKey.Challenge())
        val body = response.body<AssertionStartResponse>()

        then()
        coVerify { useCase.invoke() }
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(answer, body)
    }
}