package com.bookk.auth.microservice.route.api

import com.bookk.auth.domain.api.authentication.entity.VerifySignInRequest
import com.bookk.auth.domain.api.authentication.operation.FinishAssertion.Error.ChallengeWindowExpired
import com.bookk.auth.domain.api.authentication.operation.FinishAssertion.Error.PasskeyOwnerNotFound
import com.bookk.auth.domain.api.authentication.operation.FinishAssertion.Error.VerificationFailed
import com.bookk.auth.domain.api.authentication.operation.SignIn
import com.bookk.auth.domain.api.error.AuthErrorCodes
import com.bookk.auth.domain.api.token.entity.AuthTokens
import com.bookk.auth.microservice.route.AuthRouting
import com.bookk.core.domain.entity.SimpleServerError
import com.bookk.core.service.test.createTestClient
import com.bookk.core.service.test.routeTest
import com.bookk.core.service.test.setupApplication
import com.bookk.core.test.given
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import java.util.UUID
import kotlin.uuid.Uuid

internal class PostValidateSignInTest {

    @Test
    fun `should return OK and tokens when sign in is successful`() = routeTest {
        given()
        val useCase: SignIn = mockk()
        setupApplication(
            diModule = module {
                single { useCase }
            },
            routeUnderTest = { signIn() }
        )
        val client = createTestClient()
        val answer = AuthTokens(accessToken = "token1", refreshToken = "token2")
        val request = VerifySignInRequest(
            requestId = UUID.randomUUID().toString(),
            deviceInfo = VerifySignInRequest.DeviceInfo(
                deviceUUID = Uuid.random(),
                deviceName = ""
            ),
            publicKeyCredentialJson = "mock"
        )
        coEvery { useCase.invoke(any()) } returns Result.success(answer)

        whenn()
        val response = client.post(AuthRouting.Api.Auth.SignIn()) {
            setBody(request)
        }
        val body = response.body<AuthTokens>()

        then()
        coVerify { useCase.invoke(any()) }
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(answer, body)
    }

    @Test
    fun `should return unprocessable entity when passkey owner is not found`() = routeTest {
        given()
        val useCase: SignIn = mockk()
        setupApplication(
            diModule = module {
                single { useCase }
            },
            routeUnderTest = { signIn() }
        )
        val client = createTestClient()
        val request = VerifySignInRequest(
            requestId = UUID.randomUUID().toString(),
            deviceInfo = VerifySignInRequest.DeviceInfo(
                deviceUUID = Uuid.random(),
                deviceName = ""
            ),
            publicKeyCredentialJson = "mock"
        )
        coEvery { useCase.invoke(any()) } returns Result.failure(PasskeyOwnerNotFound)

        whenn()
        val response = client.post(AuthRouting.Api.Auth.SignIn()) {
            setBody(request)
        }
        val body = response.body<SimpleServerError>()
        then()
        coVerify { useCase.invoke(any()) }
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertEquals(AuthErrorCodes.PASSKEY_OWNER_NOT_FOUND, body.errorCode)
        assertEquals(PasskeyOwnerNotFound.message, body.message)
    }

    @Test
    fun `should return unprocessable entity when challenge window is expired`() = routeTest {
        given()
        val useCase: SignIn = mockk()
        setupApplication(
            diModule = module {
                single { useCase }
            },
            routeUnderTest = { signIn() }
        )
        val client = createTestClient()
        val request = VerifySignInRequest(
            requestId = UUID.randomUUID().toString(),
            deviceInfo = VerifySignInRequest.DeviceInfo(
                deviceUUID = Uuid.random(),
                deviceName = ""
            ),
            publicKeyCredentialJson = "mock"
        )
        coEvery { useCase.invoke(any()) } returns Result.failure(ChallengeWindowExpired)

        whenn()
        val response = client.post(AuthRouting.Api.Auth.SignIn()) {
            setBody(request)
        }
        val body = response.body<SimpleServerError>()
        then()
        coVerify { useCase.invoke(any()) }
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertEquals(AuthErrorCodes.CHALLENGE_WINDOW_EXPIRED, body.errorCode)
        assertEquals(ChallengeWindowExpired.message, body.message)
    }

    @Test
    fun `should return unprocessable entity when verification fails`() = routeTest {
        given()
        val useCase: SignIn = mockk()
        setupApplication(
            diModule = module {
                single { useCase }
            },
            routeUnderTest = { signIn() }
        )
        val client = createTestClient()
        val request = VerifySignInRequest(
            requestId = UUID.randomUUID().toString(),
            deviceInfo = VerifySignInRequest.DeviceInfo(
                deviceUUID = Uuid.random(),
                deviceName = ""
            ),
            publicKeyCredentialJson = "mock"
        )
        coEvery { useCase.invoke(any()) } returns Result.failure(VerificationFailed)

        whenn()
        val response = client.post(AuthRouting.Api.Auth.SignIn()) {
            setBody(request)
        }
        val body = response.body<SimpleServerError>()
        then()
        coVerify { useCase.invoke(any()) }
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertEquals(AuthErrorCodes.VERIFICATION_FAILED, body.errorCode)
        assertEquals(VerificationFailed.message, body.message)
    }
}