package com.bookk.auth.domain.impl.operation

import com.bookk.auth.domain.api.authentication.entity.FinishAssertionRequest
import com.bookk.auth.domain.api.authentication.operation.FinishAssertion
import com.bookk.auth.domain.api.delete_account.operation.DeleteAccount
import com.bookk.auth.domain.api.delete_account.operation.DeleteAccount.Error.InvalidCredentials
import com.bookk.auth.domain.datasource.AccountDataSource
import com.bookk.core.data.eventstreaming.StandardEventProducer
import com.bookk.core.data.eventstreaming.send
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.server.auth.client.AuthEvent
import kotlin.uuid.Uuid

internal class DeleteAccountImpl(
    private val finishAssertion: FinishAssertion,
    private val accountDataSource: AccountDataSource,
    private val eventProducer: StandardEventProducer,
    private val transactionManager: TransactionManager
) : DeleteAccount {

    override suspend fun invoke(
        userId: Uuid,
        request: FinishAssertionRequest
    ): Result<Unit> = transactionManager.transaction {
        finishAssertion(request).getOrThrow()
        val authRecord = accountDataSource.getAuthRecordByUserId(userId) ?: throw InvalidCredentials()
        eventProducer.send(AuthEvent.UserDeleted(authRecord.userId))
        accountDataSource.deleteAuthorization(authRecord.userId)
    }

}