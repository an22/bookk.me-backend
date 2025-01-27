package com.book.auth.domain.api.signout.operation

interface SignOut {

    suspend operator fun invoke(deviceId: Long): Result<Unit>

}