package com.book.user.domain.impl.operation

import com.book.user.domain.api.datasource.UserLocalDataSource
import com.book.user.domain.api.entity.User
import com.book.user.domain.api.operation.CreateUser

internal class CreateUserImpl(
    private val localDataSource: UserLocalDataSource
) : CreateUser {
    override suspend fun call(params: User): Result<Long> {
        return runCatching {
            localDataSource.insertNewUser(params)
        }
    }
}