package com.book.user.domain.impl.operation

import com.book.user.domain.api.entity.User
import com.book.user.domain.api.operation.GetUserById
import com.book.user.domain.api.operation.GetUserById.Error
import com.book.user.domain.datasource.UserDataSource

internal class GetUserByIdImpl(
    private val userDataSource: UserDataSource
) : GetUserById {

    override suspend fun invoke(userId: Long): Result<User> = runCatching {
        userDataSource.getUserById(userId) ?: throw Error.UserNotFound
    }

}