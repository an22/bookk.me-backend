package com.book.core.domain.entity

inline fun <R, reified T> Result<R>.handle(
    onSuccess: (result: R) -> Unit,
    onBusinessError: (error: T) -> Unit,
    onUnexpectedError: (error: Error) -> Unit
) {
    onSuccess {
        onSuccess(it)
    }.onFailure {
        when (it) {
            is T -> onBusinessError(it)
            is Error -> onUnexpectedError(it)
            else -> onUnexpectedError(Error.UnknownError(it))
        }
    }
}