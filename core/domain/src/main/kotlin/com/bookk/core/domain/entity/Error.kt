package com.bookk.core.domain.entity

sealed class Error(cause: Throwable) : Throwable(cause) {
    class OperationNotAllowed : Error(IllegalAccessException())
    class DatabaseError(val text: String, cause: Throwable) : Error(cause)
    class UniqueConstraintFailed(val text: String, cause: Throwable) : Error(cause)
    class UnknownError(cause: Throwable) : Error(cause)
}

fun Throwable.isConstraintFailure() = this is Error.UniqueConstraintFailed
fun Throwable.isPermissionFailure() = this is Error.OperationNotAllowed

suspend inline fun <T> Result<T>.onConstraintFailure(crossinline action: suspend () -> Nothing): Result<T> {
    return recoverCatching {
        if (it.isConstraintFailure()) action()
        else throw it
    }
}

suspend inline fun <T> Result<T>.onPermissionsMissing(crossinline action: suspend () -> Nothing): Result<T> {
    return recoverCatching {
        if (it.isPermissionFailure()) action()
        else throw it
    }
}