package com.book.user.domain.impl.operation

import com.book.user.domain.api.entity.EmailBody
import com.book.user.domain.api.entity.User
import com.book.user.domain.api.operation.GetUserByEmail
import com.book.user.domain.api.operation.GetUserByEmail.Error
import com.book.user.domain.datasource.UserDataSource

internal class GetUserByEmailImpl(
    private val userDataSource: UserDataSource
) : GetUserByEmail {

    override suspend fun invoke(body: EmailBody): Result<User> = runCatching {
        userDataSource.getUserByEmail(body.email) ?: throw Error.UserNotFound
    }

}