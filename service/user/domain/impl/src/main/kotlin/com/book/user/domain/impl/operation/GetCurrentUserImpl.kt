package com.book.user.domain.impl.operation

import com.book.user.domain.api.datasource.UserLocalDataSource
import com.book.user.domain.api.entity.User
import com.book.user.domain.api.operation.GetCurrentUser
import com.book.user.domain.api.operation.GetCurrentUser.GetCurrentUserError

internal class GetCurrentUserImpl(
    private val localDataSource: UserLocalDataSource
) : GetCurrentUser {
    override suspend fun call(params: Long): Result<User> {
        return runCatching {
            localDataSource.getUserById(params) ?: throw GetCurrentUserError.UserNotFound
        }
    }
}