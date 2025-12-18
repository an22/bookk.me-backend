package com.bookk.user.domain.impl.operation

import com.bookk.user.domain.api.entity.EmailBody
import com.bookk.user.domain.api.entity.User
import com.bookk.user.domain.api.operation.GetUserByEmail
import com.bookk.user.domain.api.operation.GetUserByEmail.Error
import com.bookk.user.domain.datasource.UserDataSource

internal class GetUserByEmailImpl(
    private val userDataSource: UserDataSource
) : GetUserByEmail {

    override suspend fun invoke(body: EmailBody): Result<User> = runCatching {
        userDataSource.getUserByEmail(body.email) ?: throw Error.UserNotFound
    }

}