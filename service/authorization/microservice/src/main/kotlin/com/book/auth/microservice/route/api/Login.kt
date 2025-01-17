package com.book.auth.microservice.route.api

import com.book.auth.domain.api.entity.SignInInfo
import com.book.auth.domain.api.entity.TokenInfo
import com.book.auth.domain.api.operation.GenerateAuthToken
import com.book.auth.microservice.route.AuthRouting.Api
import com.book.core.service.applyMediaType
import com.book.core.service.enity.SimpleServerError
import com.book.core.service.enity.respondWith
import io.bkbn.kompendium.core.metadata.PostInfo
import io.bkbn.kompendium.resources.NotarizedResource
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.resources.post
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import org.koin.ktor.ext.inject

internal fun Route.postLogin() {
    withSignInDocumentation()
    post<Api.Auth.SignIn> {
        val info = call.receive<SignInInfo>()
        val generateToken by application.inject<GenerateAuthToken>()
        call.respondWith(generateToken(GenerateAuthToken.Source.FromPublicKey(info)))
    }
}

internal fun Route.withSignInDocumentation() {
    install(NotarizedResource<Api.Auth.SignIn>()) {
        tags = setOf("auth")
        post = PostInfo.builder {
            summary("Sign In")
            description("Get access token and refresh token.")
            request {
                applyMediaType()
                requestType<SignInInfo>()
                description("User auth credentials and current device name")
            }
            response {
                applyMediaType()
                responseCode(HttpStatusCode.OK)
                responseType<TokenInfo>()
                description("Access token and refresh token")
            }
            canRespond {
                applyMediaType()
                responseCode(HttpStatusCode.BadRequest)
                responseType<SimpleServerError>()
                description("User not found for provided credentials")
            }
        }
    }
}