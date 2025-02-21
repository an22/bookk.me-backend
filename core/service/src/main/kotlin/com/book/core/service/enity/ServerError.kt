package com.book.core.service.enity

import com.book.core.domain.entity.BusinessError
import com.book.core.domain.entity.SimpleServerError
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.RoutingCall

suspend inline fun <reified R: Any> RoutingCall.respondWith(result: Result<R>) {
    result
        .onSuccess {
            respond(
                status = HttpStatusCode.OK,
                message = it
            )
        }
        .onFailure { error ->
            when (error) {
                is BusinessError -> {
                    respond(
                        status = HttpStatusCode.fromValue(error.statusCode),
                        message = SimpleServerError(error.message, error.code)
                    )
                }
                else -> respond(HttpStatusCode.InternalServerError)
            }
        }
}