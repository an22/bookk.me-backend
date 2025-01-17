package com.book.core.service.enity

import com.book.core.domain.entity.BusinessError
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.RoutingCall
import kotlinx.serialization.Serializable

@Serializable
class SimpleServerError(
    val message: String,
    val errorCode: Int
)

@Serializable
class MessageServerError(
    val message: String
)

@Serializable
class BodyServerError<T : Any>(
    val message: String,
    val errorCode: Int,
    val errorBody: T
)

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