package com.book.auth.microservice.route.api

import com.book.auth.domain.api.entity.TokenInfo
import com.book.auth.domain.api.entity.VerifyAccountCreationRequest
import com.book.auth.domain.api.operation.FinishRegistration
import com.book.auth.domain.api.operation.FinishRegistration.Error.AccountCreationFailed
import com.book.auth.domain.api.operation.FinishRegistration.Error.InvalidEmailFormat
import com.book.auth.domain.api.operation.FinishRegistration.Error.UserAlreadyExist
import com.book.auth.domain.api.operation.FinishRegistration.Error.VerificationFailed
import com.book.auth.microservice.route.AuthRouting
import com.book.core.service.enity.SimpleServerError
import com.bookk.core.service.test.createTestClient
import com.bookk.core.service.test.serverTest
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

class PostValidateRegistrationTest {

    private fun createSimpleRequest(): VerifyAccountCreationRequest {
        return VerifyAccountCreationRequest(
            deviceInfo = VerifyAccountCreationRequest.DeviceInfo(
                deviceUUID = "uuid",
                deviceName = "example_device_name"
            ),
            userInfo = VerifyAccountCreationRequest.UserInfo(
                userId = "user_id",
                name = "name",
                lastName = "lastName",
                email = "email"
            ),
            publicKeyCredentialJson = "mock"
        )
    }

    @Test
    fun invalidEmailFormat() = serverTest {
        given()
        val useCase: FinishRegistration = mockk()
        val client = createTestClient()
        val request = createSimpleRequest()
        coEvery { useCase.invoke(any()) } returns Result.failure(InvalidEmailFormat)
        setupApplication(
            diModule = module {
                single<FinishRegistration> { useCase }
            },
            routeUnderTest = { postValidateRegistration() }
        )
        whenn()
        val response = client.post(AuthRouting.Api.Auth.SignUp.PassKey.Validate()) {
            setBody(request)
        }
        val body = response.body<SimpleServerError>()
        then()
        coVerify { useCase.invoke(eq(request)) }
        assertEquals(InvalidEmailFormat.statusCode, response.status.value)
        assertEquals(InvalidEmailFormat.code, body.errorCode)
        assertEquals(InvalidEmailFormat.message, body.message)
    }

    @Test
    fun userAlreadyExist() = serverTest {
        given()
        val useCase: FinishRegistration = mockk()
        val client = createTestClient()
        val request = createSimpleRequest()
        coEvery { useCase.invoke(any()) } returns Result.failure(UserAlreadyExist)
        setupApplication(
            diModule = module {
                single<FinishRegistration> { useCase }
            },
            routeUnderTest = { postValidateRegistration() }
        )
        whenn()
        val response = client.post(AuthRouting.Api.Auth.SignUp.PassKey.Validate()) {
            setBody(request)
        }
        val body = response.body<SimpleServerError>()
        then()
        coVerify { useCase.invoke(eq(request)) }
        assertEquals(UserAlreadyExist.statusCode, response.status.value)
        assertEquals(UserAlreadyExist.code, body.errorCode)
        assertEquals(UserAlreadyExist.message, body.message)
    }

    @Test
    fun verificationFailed() = serverTest {
        given()
        val useCase: FinishRegistration = mockk()
        val client = createTestClient()
        val request = createSimpleRequest()
        coEvery { useCase.invoke(any()) } returns Result.failure(VerificationFailed)
        setupApplication(
            diModule = module {
                single<FinishRegistration> { useCase }
            },
            routeUnderTest = { postValidateRegistration() }
        )
        whenn()
        val response = client.post(AuthRouting.Api.Auth.SignUp.PassKey.Validate()) {
            setBody(request)
        }
        val body = response.body<SimpleServerError>()
        then()
        coVerify { useCase.invoke(eq(request)) }
        assertEquals(VerificationFailed.statusCode, response.status.value)
        assertEquals(VerificationFailed.code, body.errorCode)
        assertEquals(VerificationFailed.message, body.message)
    }

    @Test
    fun accountCreationFailed() = serverTest {
        given()
        val useCase: FinishRegistration = mockk()
        val client = createTestClient()
        val request = createSimpleRequest()
        coEvery { useCase.invoke(any()) } returns Result.failure(AccountCreationFailed)
        setupApplication(
            diModule = module {
                single<FinishRegistration> { useCase }
            },
            routeUnderTest = { postValidateRegistration() }
        )
        whenn()
        val response = client.post(AuthRouting.Api.Auth.SignUp.PassKey.Validate()) {
            setBody(request)
        }
        val body = response.body<SimpleServerError>()
        then()
        coVerify { useCase.invoke(eq(request)) }
        assertEquals(AccountCreationFailed.statusCode, response.status.value)
        assertEquals(AccountCreationFailed.code, body.errorCode)
        assertEquals(AccountCreationFailed.message, body.message)
    }

    @Test
    fun successResponse() = serverTest {
        given()
        val useCase: FinishRegistration = mockk()
        val client = createTestClient()
        val request = createSimpleRequest()
        val expected = TokenInfo("access_token", "refresh_token")
        coEvery { useCase.invoke(any()) } returns Result.success(expected)
        setupApplication(
            diModule = module {
                single<FinishRegistration> { useCase }
            },
            routeUnderTest = { postValidateRegistration() }
        )
        whenn()
        val response = client.post(AuthRouting.Api.Auth.SignUp.PassKey.Validate()) {
            setBody(request)
        }
        val body = response.body<TokenInfo>()
        then()
        coVerify { useCase.invoke(eq(request)) }
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(expected, body)
    }

}