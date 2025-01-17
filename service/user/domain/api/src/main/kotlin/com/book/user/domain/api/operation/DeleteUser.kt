package com.book.user.domain.api.operation

interface DeleteUser {

    suspend operator fun invoke(userId: Long): Result<Unit>

}