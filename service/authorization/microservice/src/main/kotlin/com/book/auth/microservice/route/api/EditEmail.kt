package com.book.auth.microservice.route.api

import com.book.auth.domain.api.authentication.entity.EditEmailRequest
import com.book.auth.domain.api.authentication.operation.EditEmail
import com.book.auth.microservice.route.AuthRouting.Api
import com.book.auth.microservice.route.api.documentation.common.challengeVerificationEnrichment
import com.book.core.service.auth.AppPrincipal
import com.book.core.service.enity.respondWith
import io.bkbn.kompendium.core.metadata.PostInfo
import io.bkbn.kompendium.resources.NotarizedResource
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.resources.post
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import org.koin.ktor.ext.inject

internal fun Route.postEditEmail() {
    withEditEmailDocumentation()
    authenticate {
        post<Api.Auth.Email> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val request = call.receive<EditEmailRequest>()
            val editEmail: EditEmail by application.inject()
            call.respondWith(editEmail(principal.authId, request))
        }
    }
}

internal fun Route.withEditEmailDocumentation() {
    install(NotarizedResource<Api.Auth.Email>()) {
        tags = setOf("auth")
        post = PostInfo.builder {
            summary("Edit email")
            description("Check service health")
            response {
                responseCode(HttpStatusCode.OK)
                responseType<Unit>()
                description("Service is alive")
            }
            canRespond {
                challengeVerificationEnrichment()
            }
        }
    }
}