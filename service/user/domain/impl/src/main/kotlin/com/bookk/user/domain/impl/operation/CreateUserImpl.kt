package com.bookk.user.domain.impl.operation

import com.bookk.user.domain.api.entity.User
import com.bookk.user.domain.api.entity.UserId
import com.bookk.user.domain.api.operation.CreateUser
import com.bookk.user.domain.datasource.UserDataSource

internal class CreateUserImpl(
    private val userDataSource: UserDataSource
) : CreateUser {

    override suspend fun invoke(user: User): Result<UserId> = runCatching {
        UserId(userDataSource.insertNewUser(user).id)
    }

}