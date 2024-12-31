package com.book.user.domain.impl.operation

import com.book.user.domain.api.datasource.UserLocalDataSource
import com.book.user.domain.api.entity.User
import com.book.user.domain.api.operation.GetUserById
import com.book.user.domain.api.operation.GetUserById.GetCurrentUserError

internal class GetUserByIdImpl(
    private val localDataSource: UserLocalDataSource
) : GetUserById {
    override suspend fun call(params: Long): Result<User> {
        return runCatching {
            localDataSource.getUserById(params) ?: throw GetCurrentUserError.UserNotFound
        }
    }
}