package com.book.auth.domain.impl.operation

import com.book.auth.domain.api.entity.DeleteAccountInfo
import com.book.auth.domain.api.operation.DeleteAccount
import com.book.auth.domain.api.operation.DeleteAccount.DeleteAccountError.InvalidCredentials
import com.book.auth.domain.api.operation.DeleteAccount.DeleteAccountError.UnableToDeleteAccount
import com.book.auth.domain.datasource.AccountDataSource
import com.bookk.server.user.client.UserClient

internal class DeleteAccountImpl(
    private val accountDataSource: AccountDataSource,
    private val userClient: UserClient
) : DeleteAccount {


    override suspend fun invoke(userId: Long, info: DeleteAccountInfo): Result<Unit> = runCatching {
        val authRecord = accountDataSource.getAuthRecordByUserId(userId) ?: throw InvalidCredentials
        val isCredentialsInvalid = false
        if (isCredentialsInvalid) throw InvalidCredentials
        userClient.deleteUser(authRecord.userId).getOrThrow()
        accountDataSource.deleteAuthorization(authRecord.userId)
    }.recoverCatching {
        throw UnableToDeleteAccount
    }

}