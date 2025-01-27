package com.book.user.domain.impl.operation

import com.book.user.domain.api.entity.User
import com.book.user.domain.api.entity.UserId
import com.book.user.domain.api.operation.CreateUser
import com.book.user.domain.datasource.UserDataSource

internal class CreateUserImpl(
    private val userDataSource: UserDataSource
) : CreateUser {

    override suspend fun invoke(user: User): Result<UserId> = runCatching {
        UserId(userDataSource.insertNewUser(user).id)
    }

}