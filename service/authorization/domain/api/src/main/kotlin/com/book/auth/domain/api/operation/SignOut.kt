package com.book.auth.domain.api.operation

interface SignOut {

    suspend operator fun invoke(deviceId: Long): Result<Unit>

}