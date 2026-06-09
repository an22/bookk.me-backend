package com.bookk.user.domain.impl.operation

import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.user.domain.api.entity.UserEditModel
import com.bookk.user.domain.api.operation.EditUser
import com.bookk.user.domain.api.operation.EditUser.Error
import com.bookk.user.domain.datasource.UserDataSource
import kotlin.uuid.Uuid

internal class EditUserImpl(
    private val userDataSource: UserDataSource,
    private val transactionManager: TransactionManager
) : EditUser {
    override suspend fun invoke(id: Uuid, user: UserEditModel): Result<Unit> = transactionManager.transaction {
        if (!userDataSource.updateUser(id, user)) throw Error.UserNotFound()
    }
}