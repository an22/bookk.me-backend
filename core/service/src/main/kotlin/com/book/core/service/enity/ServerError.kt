package com.book.core.service.enity

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