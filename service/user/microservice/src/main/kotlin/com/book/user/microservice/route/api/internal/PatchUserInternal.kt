package com.book.user.microservice.route.api.internal

import com.book.core.service.applyMediaType
import com.book.core.service.enity.SimpleServerError
import com.book.core.service.enity.respondWith
import com.book.user.domain.api.entity.ShortUserEditModel
import com.book.user.domain.api.entity.UserEditModel
import com.book.user.domain.api.operation.EditUser
import com.book.user.microservice.route.UserRouting.Api
import io.bkbn.kompendium.core.metadata.PatchInfo
import io.bkbn.kompendium.enrichment.NumberEnrichment
import io.bkbn.kompendium.enrichment.ObjectEnrichment
import io.bkbn.kompendium.resources.NotarizedResource
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.resources.patch
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import org.koin.ktor.ext.inject

internal fun Route.patchUserInternal() {
    withPatchUserDocumentation()
    patch<Api.Internal.User.Edit> { path ->
        val body = call.receive<UserEditModel>()
        val editUser by application.inject<EditUser>()

        call.respondWith(editUser(path.id, body))
    }
}

internal fun Route.withPatchUserDocumentation() {
    install(NotarizedResource<Api.Internal.User.Edit>()) {
        tags = setOf("user")
        patch = PatchInfo.builder {
            security = mapOf("jwt" to emptyList())
            summary("Update user")
            description("Update current user")
            request {
                required(true)
                requestType<ShortUserEditModel>()
                description("Fields that needs to be updated")
            }
            response {
                applyMediaType()
                responseCode(HttpStatusCode.OK)
                responseType<Unit>()
                description("User successfully updated")
            }
            canRespond {
                applyMediaType()
                responseCode(HttpStatusCode.NotFound)
                responseType<SimpleServerError>(ObjectEnrichment(id = "EditUser") {
                    SimpleServerError::errorCode {
                        NumberEnrichment("errorCode") {
                            minimum = 1
                            maximum = 1
                            description = buildString {
                                append("${EditUser.Error.UserNotFound.code} - ${EditUser.Error.UserNotFound.message}\n")
                            }
                        }
                    }
                })
                description("User not found")
            }
        }
    }
}