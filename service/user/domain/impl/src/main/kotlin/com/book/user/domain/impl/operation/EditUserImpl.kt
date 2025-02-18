package com.book.user.domain.impl.operation

import com.book.user.domain.api.entity.UserEditModel
import com.book.user.domain.api.operation.EditUser
import com.book.user.domain.api.operation.EditUser.Error
import com.book.user.domain.datasource.UserDataSource

internal class EditUserImpl(
    private val userDataSource: UserDataSource
) : EditUser {
    override suspend fun invoke(id: Long, user: UserEditModel): Result<Unit> = runCatching {
        if (!userDataSource.updateUser(id, user)) throw Error.UserNotFound
    }
}