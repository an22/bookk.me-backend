package com.book.user.microservice.route.api

import com.book.core.domain.entity.handle
import com.book.core.service.enity.MessageServerError
import com.book.user.domain.api.operation.DeleteUser
import com.book.user.domain.api.routing.UserRouting.Api
import io.bkbn.kompendium.core.metadata.DeleteInfo
import io.bkbn.kompendium.resources.NotarizedResource
import io.ktor.http.HttpStatusCode
import io.ktor.server.resources.delete
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import org.koin.ktor.ext.inject

internal fun Route.deleteUser() {
    withDeleteUserDocumentation()
    delete<Api.Internal.User.Delete> { user ->
        val operation by application.inject<DeleteUser>()
        operation.call(DeleteUser.Param(user.id))
            .handle<_, Unit>(
                onSuccess = {
                    call.respond(HttpStatusCode.OK)
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

internal fun Route.withDeleteUserDocumentation() {
    install(NotarizedResource<Api.Internal.User.Delete>()) {
        tags = setOf("internal", "user")
        delete = DeleteInfo.builder {
            summary("Delete user")
            description("Delete user from the system. This action is permanent and can't be reversed.")
            response {
                responseCode(HttpStatusCode.OK)
                responseType<Unit>()
                description("User deleted")
            }
        }
    }
}