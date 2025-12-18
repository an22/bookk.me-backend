package com.bookk.user.domain.impl.operation

import com.bookk.user.domain.api.operation.DeleteUser
import com.bookk.user.domain.datasource.UserDataSource
import kotlin.uuid.Uuid

internal class DeleteUserImpl(
    private val userDataSource: UserDataSource
) : DeleteUser {

    override suspend fun invoke(userId: Uuid): Result<Unit> = runCatching {
        userDataSource.deleteUser(userId)
    }

}