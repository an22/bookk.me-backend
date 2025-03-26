package com.book.auth.microservice.route.api

import com.book.auth.domain.api.authentication.entity.AssertionStartResponse
import com.book.auth.domain.api.authentication.operation.StartAssertion
import com.book.auth.microservice.route.AuthRouting.Api
import com.book.core.service.applyMediaType
import com.book.core.service.enity.respondWith
import io.bkbn.kompendium.core.metadata.GetInfo
import io.bkbn.kompendium.resources.NotarizedResource
import io.ktor.http.HttpStatusCode
import io.ktor.server.resources.get
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import org.koin.ktor.ext.inject

internal fun Route.getVerificationChallenge() {
    withVerificationDocumentation()
    get<Api.Auth.PassKey.Challenge> {
        val startAssertion: StartAssertion by application.inject()
        call.respondWith(startAssertion())
    }
}

internal fun Route.withVerificationDocumentation() {
    install(NotarizedResource<Api.Auth.PassKey.Challenge>()) {
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