package com.book.auth.microservice.route.api

import com.book.auth.domain.api.authentication.entity.VerifySignInRequest
import com.book.auth.domain.api.authentication.operation.SignIn
import com.book.auth.domain.api.authentication.operation.SignIn.Error.ChallengeWindowExpired
import com.book.auth.domain.api.authentication.operation.SignIn.Error.PasskeyOwnerNotFound
import com.book.auth.domain.api.authentication.operation.SignIn.Error.VerificationFailed
import com.book.auth.domain.api.error.AuthErrorCodes
import com.book.auth.domain.api.token.entity.AuthTokens
import com.book.auth.microservice.route.AuthRouting
import com.book.core.service.enity.SimpleServerError
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

internal class PostValidateSignInTest {

    @Test
    fun verifyAnswerOnSuccess() = routeTest {
        given()
        val useCase: SignIn = mockk()
        setupApplication(
            diModule = module {
                single { useCase }
            },
            routeUnderTest = { postSignIn() }
        )
        val client = createTestClient()
        val answer = AuthTokens(accessToken = "token1", refreshToken = "token2")
        val request = VerifySignInRequest(
            requestId = UUID.randomUUID().toString(),
            deviceInfo = VerifySignInRequest.DeviceInfo(
                deviceUUID = UUID.randomUUID().toString(),
                deviceName = ""
            ),
            publicKeyCredentialJson = "mock"
        )
        coEvery { useCase.invoke(any()) } returns Result.success(answer)

        whenn()
        val response = client.post(AuthRouting.Api.Auth.SignIn.PassKey.Validate()) {
            setBody(request)
        }
        val body = response.body<AuthTokens>()

        then()
        coVerify { useCase.invoke(any()) }
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(answer, body)
    }

    @Test
    fun verifyPasskeyOwnerNotFound() = routeTest {
        given()
        val useCase: SignIn = mockk()
        setupApplication(
            diModule = module {
                single { useCase }
            },
            routeUnderTest = { postSignIn() }
        )
        val client = createTestClient()
        val request = VerifySignInRequest(
            requestId = UUID.randomUUID().toString(),
            deviceInfo = VerifySignInRequest.DeviceInfo(
                deviceUUID = UUID.randomUUID().toString(),
                deviceName = ""
            ),
            publicKeyCredentialJson = "mock"
        )
        coEvery { useCase.invoke(any()) } returns Result.failure(PasskeyOwnerNotFound)

        whenn()
        val response = client.post(AuthRouting.Api.Auth.SignIn.PassKey.Validate()) {
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
    fun verifyChallengeWindowExpired() = routeTest {
        given()
        val useCase: SignIn = mockk()
        setupApplication(
            diModule = module {
                single { useCase }
            },
            routeUnderTest = { postSignIn() }
        )
        val client = createTestClient()
        val request = VerifySignInRequest(
            requestId = UUID.randomUUID().toString(),
            deviceInfo = VerifySignInRequest.DeviceInfo(
                deviceUUID = UUID.randomUUID().toString(),
                deviceName = ""
            ),
            publicKeyCredentialJson = "mock"
        )
        coEvery { useCase.invoke(any()) } returns Result.failure(ChallengeWindowExpired)

        whenn()
        val response = client.post(AuthRouting.Api.Auth.SignIn.PassKey.Validate()) {
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
    fun verifyVerificationFailed() = routeTest {
        given()
        val useCase: SignIn = mockk()
        setupApplication(
            diModule = module {
                single { useCase }
            },
            routeUnderTest = { postSignIn() }
        )
        val client = createTestClient()
        val request = VerifySignInRequest(
            requestId = UUID.randomUUID().toString(),
            deviceInfo = VerifySignInRequest.DeviceInfo(
                deviceUUID = UUID.randomUUID().toString(),
                deviceName = ""
            ),
            publicKeyCredentialJson = "mock"
        )
        coEvery { useCase.invoke(any()) } returns Result.failure(VerificationFailed)

        whenn()
        val response = client.post(AuthRouting.Api.Auth.SignIn.PassKey.Validate()) {
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