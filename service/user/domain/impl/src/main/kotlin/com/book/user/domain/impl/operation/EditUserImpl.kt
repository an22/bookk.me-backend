package com.book.user.domain.impl.operation

import com.book.user.domain.api.entity.User
import com.book.user.domain.api.operation.EditUser
import com.book.user.domain.datasource.UserDataSource

internal class EditUserImpl(
    private val userDataSource: UserDataSource
) : EditUser {
    override suspend fun invoke(user: User): Result<Unit> = runCatching {
        if (!userDataSource.updateUser(user)) {
            throw EditUser.Error.UserNotFound
        }
    }
}