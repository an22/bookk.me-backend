package com.book.user.domain.impl.operation

import com.book.user.domain.api.datasource.UserLocalDataSource
import com.book.user.domain.api.operation.DeleteUser

internal class DeleteUserImpl(
    private val userLocalDataSource: UserLocalDataSource
) : DeleteUser {
    override suspend fun call(params: DeleteUser.Param): Result<Unit> = runCatching {
        userLocalDataSource.deleteUser(params.userId)
    }
}