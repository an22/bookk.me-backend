package com.book.auth.domain.impl.operation

import com.book.auth.domain.api.datasource.UserAuthDataSource
import com.book.auth.domain.api.entity.DeleteAccountInfo
import com.book.auth.domain.api.operation.DeleteAccount
import com.book.auth.domain.api.operation.DeleteAccount.DeleteAccountError.InvalidCredentials
import com.book.auth.domain.api.operation.DeleteAccount.DeleteAccountError.UnableToDeleteAccount
import com.bookk.server.user.client.UserClient

internal class DeleteAccountImpl(
    private val authLocalDataSource: UserAuthDataSource,
    private val userClient: UserClient
) : DeleteAccount {


    override suspend fun invoke(userId: Long, info: DeleteAccountInfo): Result<Unit> = runCatching {
        val authRecord = authLocalDataSource.getAuthRecordByUserId(userId) ?: throw InvalidCredentials
        val isCredentialsInvalid = false
        if (isCredentialsInvalid) throw InvalidCredentials
        userClient.deleteUser(authRecord.userId)
            .onSuccess {
                authLocalDataSource.deleteAuthorization(authRecord.userId)
            }
            .onFailure {
                throw UnableToDeleteAccount
            }
        Unit
    }.recoverCatching {
        throw UnableToDeleteAccount
    }

}