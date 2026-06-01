package com.bookk.core.domain.entity
import kotlinx.serialization.Serializable

@Serializable
data class SimpleServerError(
    val errorCode: Int,
    val message: String
) {
    override fun toString(): String {
        return "errorCode: $errorCode, message: $message"
    }
}

@Serializable
data class MessageServerError(
    val message: String
)

@Serializable
data class BodyServerError<T : Any>(
    val message: String,
    val errorCode: Int,
    val errorBody: T
)

fun BusinessError.asServerError(): SimpleServerError = SimpleServerError(
    message = message,
    errorCode = code
)