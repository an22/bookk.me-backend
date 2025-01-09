package com.book.user.domain.impl.operation

import com.book.user.domain.api.datasource.UserDataSource
import com.book.user.domain.api.entity.User
import com.book.user.domain.api.operation.GetUserById
import com.book.user.domain.api.operation.GetUserById.GetCurrentUserError

internal class GetUserByIdImpl(
    private val localDataSource: UserDataSource
) : GetUserById {

    override suspend fun invoke(userId: Long): Result<User> = runCatching {
        localDataSource.getUserById(userId) ?: throw GetCurrentUserError.UserNotFound
    }

}