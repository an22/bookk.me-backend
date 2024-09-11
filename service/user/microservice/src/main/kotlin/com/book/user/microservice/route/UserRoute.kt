package com.book.user.microservice.route

import com.book.core.domain.entity.handle
import com.book.core.service.auth.AppPrincipal
import com.book.core.service.enity.MessageServerError
import com.book.core.service.enity.SimpleServerError
import com.book.user.domain.api.operation.GetCurrentUser
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject


fun Routing.userRoute() {
    authenticate {
        route("/user") {
            createUserDocumentation()
            get {
                val operation by application.inject<GetCurrentUser>()
                val principal = requireNotNull(call.principal<AppPrincipal>())
                operation.call(principal.userId)
                    .handle<_, GetCurrentUser.GetCurrentUserError>(
                        onSuccess = {
                            call.respond(it)
                        },
                        onBusinessError = {
                            call.respond(HttpStatusCode.NotFound, SimpleServerError(it.message.orEmpty(), it.code))
                        },
                        onUnexpectedError = {
                            call.respond(HttpStatusCode.InternalServerError, MessageServerError(it.message.orEmpty()))
                        }
                    )

            }
        }
    }
}