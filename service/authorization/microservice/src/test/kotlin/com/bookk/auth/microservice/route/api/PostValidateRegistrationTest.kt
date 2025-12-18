package com.bookk.auth.microservice.route.api

import com.bookk.auth.domain.api.error.AuthErrorCodes
import com.bookk.auth.domain.api.registration.entity.VerifyAccountCreationRequest
import com.bookk.auth.domain.api.registration.operation.FinishPasskeyRegistration.Error.VerificationFailed
import com.bookk.auth.domain.api.registration.operation.FinishRegistration
import com.bookk.auth.domain.api.registration.operation.FinishRegistration.Error.AccountCreationFailed
import com.bookk.auth.domain.api.registration.operation.FinishRegistration.Error.InvalidEmailFormat
import com.bookk.auth.domain.api.registration.operation.FinishRegistration.Error.UserAlreadyExist
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

class registrationTest {

    private fun createSimpleRequest(): VerifyAccountCreationRequest {
        return VerifyAccountCreationRequest(
            requestId = "user_id",
            deviceInfo = VerifyAccountCreationRequest.DeviceInfo(
                deviceUUID = "uuid",
                deviceName = "example_device_name"
            ),
            userInfo = VerifyAccountCreationRequest.UserInfo(
                name = "name",
                lastName = "lastName",
                email = "email"
            ),
            publicKeyCredentialJson = "mock"
        )
    }

    @Test
    fun invalidEmailFormat() = routeTest {
        given()
        val useCase: FinishRegistration = mockk()
        val client = createTestClient()
        val request = createSimpleRequest()
        coEvery { useCase.invoke(any()) } returns Result.failure(InvalidEmailFormat)
        setupApplication(
            diModule = module {
                single<FinishRegistration> { useCase }
            },
            routeUnderTest = { registration() }
        )
        whenn()
        val response = client.post(AuthRouting.Api.Auth.SignUp()) {
            setBody(request)
        }
        val body = response.body<SimpleServerError>()
        then()
        coVerify { useCase.invoke(eq(request)) }
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertEquals(AuthErrorCodes.INVALID_EMAIL_FORMAT, body.errorCode)
        assertEquals(InvalidEmailFormat.message, body.message)
    }

    @Test
    fun userAlreadyExist() = routeTest {
        given()
        val useCase: FinishRegistration = mockk()
        val client = createTestClient()
        val request = createSimpleRequest()
        coEvery { useCase.invoke(any()) } returns Result.failure(UserAlreadyExist)
        setupApplication(
            diModule = module {
                single<FinishRegistration> { useCase }
            },
            routeUnderTest = { registration() }
        )
        whenn()
        val response = client.post(AuthRouting.Api.Auth.SignUp()) {
            setBody(request)
        }
        val body = response.body<SimpleServerError>()
        then()
        coVerify { useCase.invoke(eq(request)) }
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertEquals(AuthErrorCodes.USER_ALREADY_EXIST, body.errorCode)
        assertEquals(UserAlreadyExist.message, body.message)
    }

    @Test
    fun verificationFailed() = routeTest {
        given()
        val useCase: FinishRegistration = mockk()
        val client = createTestClient()
        val request = createSimpleRequest()
        coEvery { useCase.invoke(any()) } returns Result.failure(VerificationFailed)
        setupApplication(
            diModule = module {
                single<FinishRegistration> { useCase }
            },
            routeUnderTest = { registration() }
        )
        whenn()
        val response = client.post(AuthRouting.Api.Auth.SignUp()) {
            setBody(request)
        }
        val body = response.body<SimpleServerError>()
        then()
        coVerify { useCase.invoke(eq(request)) }
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertEquals(AuthErrorCodes.VERIFICATION_FAILED, body.errorCode)
        assertEquals(VerificationFailed.message, body.message)
    }

    @Test
    fun accountCreationFailed() = routeTest {
        given()
        val useCase: FinishRegistration = mockk()
        val client = createTestClient()
        val request = createSimpleRequest()
        coEvery { useCase.invoke(any()) } returns Result.failure(AccountCreationFailed)
        setupApplication(
            diModule = module {
                single<FinishRegistration> { useCase }
            },
            routeUnderTest = { registration() }
        )
        whenn()
        val response = client.post(AuthRouting.Api.Auth.SignUp()) {
            setBody(request)
        }
        val body = response.body<SimpleServerError>()
        then()
        coVerify { useCase.invoke(eq(request)) }
        assertEquals(HttpStatusCode.InternalServerError, response.status)
        assertEquals(AuthErrorCodes.ACCOUNT_CREATION_FAILED, body.errorCode)
        assertEquals(AccountCreationFailed.message, body.message)
    }

    @Test
    fun successResponse() = routeTest {
        given()
        val useCase: FinishRegistration = mockk()
        val client = createTestClient()
        val request = createSimpleRequest()
        val expected = AuthTokens("access_token", "refresh_token")
        coEvery { useCase.invoke(any()) } returns Result.success(expected)
        setupApplication(
            diModule = module {
                single<FinishRegistration> { useCase }
            },
            routeUnderTest = { registration() }
        )
        whenn()
        val response = client.post(AuthRouting.Api.Auth.SignUp()) {
            setBody(request)
        }
        val body = response.body<AuthTokens>()
        then()
        coVerify { useCase.invoke(eq(request)) }
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(expected, body)
    }

}