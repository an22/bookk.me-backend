package com.book.auth.microservice.route.api

import com.book.auth.domain.api.authentication.entity.VerifySignInRequest
import com.book.auth.domain.api.authentication.operation.SignIn
import com.book.auth.domain.api.token.entity.AuthTokens
import com.book.auth.microservice.route.AuthRouting.Api
import com.book.auth.microservice.route.api.documentation.common.challengeVerificationEnrichment
import com.book.core.service.applyMediaType
import com.book.core.service.enity.respondWith
import io.bkbn.kompendium.core.metadata.PostInfo
import io.bkbn.kompendium.resources.NotarizedResource
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.resources.post
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import org.koin.ktor.ext.inject

internal fun Route.postSignIn() {
    withSignInDocumentation()
    post<Api.Auth.SignIn> {
        val request = call.receive<VerifySignInRequest>()
        val startSigningIn: SignIn by application.inject()
        call.respondWith(startSigningIn(request))
    }
}

internal fun Route.withSignInDocumentation() {
    install(NotarizedResource<Api.Auth.SignIn>()) {
        tags = setOf("auth")
        post = PostInfo.builder {
            summary("Verify sign in")
            description("Verify user-signed challenge and return authentication tokens")
            request {
                applyMediaType()
                requestType<VerifySignInRequest>()
                description("User parameters")
            }
            response {
                applyMediaType()
                responseCode(HttpStatusCode.OK)
                responseType<AuthTokens>()
                description("Authentication tokens")
            }
            canRespond {
                challengeVerificationEnrichment()
            }
        }
    }
}