package com.book.user.domain.impl.operation

import com.book.user.domain.api.datasource.UserDataSource
import com.book.user.domain.api.entity.User
import com.book.user.domain.api.operation.CreateUser

internal class CreateUserImpl(
    private val localDataSource: UserDataSource
) : CreateUser {

    override suspend fun invoke(user: User): Result<Long> = runCatching {
        localDataSource.insertNewUser(user).id
    }

}