package com.book.auth.microservice.route.api

import com.book.auth.domain.api.authentication.entity.AssertionStartResponse
import com.book.auth.domain.api.authentication.entity.VerifySignInRequest
import com.book.auth.domain.api.authentication.operation.SignIn
import com.book.auth.domain.api.authentication.operation.StartAssertion
import com.book.auth.domain.api.token.entity.AuthTokens
import com.book.auth.microservice.route.AuthRouting.Api
import com.book.auth.microservice.route.api.documentation.common.challengeVerificationEnrichment
import com.book.core.service.applyMediaType
import com.book.core.service.enity.respondWith
import io.bkbn.kompendium.core.metadata.GetInfo
import io.bkbn.kompendium.core.metadata.PostInfo
import io.bkbn.kompendium.resources.NotarizedResource
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import org.koin.ktor.ext.inject

internal fun Route.signIn() {
    withSignInDocumentation()
    withSignInVerificationDocumentation()
    get<Api.Auth.PassKey.SignInChallenge> {
        val startAssertion: StartAssertion by application.inject()
        call.respondWith(startAssertion())
    }
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

private fun Route.withSignInVerificationDocumentation() {
    install(NotarizedResource<Api.Auth.PassKey.SignInChallenge>()) {
        tags = setOf("auth")
        get = GetInfo.builder {
            summary("Get sign in challenge")
            description("Get sign in passkey challenge.")
            response {
                applyMediaType()
                responseCode(HttpStatusCode.OK)
                responseType<AssertionStartResponse>()
                description("Json payload")
            }
        }
    }
}