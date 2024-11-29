package com.book.user.microservice.route.api

import com.book.core.domain.entity.handle
import com.book.core.service.applyMediaType
import com.book.core.service.enity.MessageServerError
import com.book.user.domain.api.entity.UserExistInfo
import com.book.user.domain.api.entity.UserIdentity
import com.book.user.domain.api.operation.IsUserExistWithParameters
import com.book.user.domain.api.routing.UserRouting.Api
import io.bkbn.kompendium.core.metadata.PostInfo
import io.bkbn.kompendium.resources.NotarizedResource
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import org.koin.ktor.ext.inject

internal fun Route.postCheckExistence() {
    withUserExistsDocumentation()
    post<Api.Internal.User.Exist> {
        val operation by application.inject<IsUserExistWithParameters>()
        val identity = call.receive<UserIdentity>()
        operation.call(IsUserExistWithParameters.Param(identity.phone, identity.email))
            .handle<_, Unit>(
                onSuccess = {
                    call.respond(UserExistInfo(it))
                },
                onBusinessError = {
                    call.respond(HttpStatusCode.InternalServerError)
                },
                onUnexpectedError = {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        MessageServerError(it.message.orEmpty())
                    )
                }
            )
    }
}


internal fun Route.withUserExistsDocumentation() {
    install(NotarizedResource<Api.Internal.User.Exist>()) {
        tags = setOf("internal", "user")
        post = PostInfo.builder {
            summary("Check user existence")
            description("Check if user exists with provided email and phone")
            response {
                applyMediaType()
                responseCode(HttpStatusCode.OK)
                responseType<UserExistInfo>()
                description("True if user exists, false otherwise")
            }
        }
    }
}