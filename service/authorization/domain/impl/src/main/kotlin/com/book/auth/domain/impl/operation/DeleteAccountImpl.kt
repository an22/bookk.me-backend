package com.book.auth.domain.impl.operation

import com.book.auth.domain.api.authentication.entity.FinishAssertionRequest
import com.book.auth.domain.api.authentication.operation.FinishAssertion
import com.book.auth.domain.api.delete_account.operation.DeleteAccount
import com.book.auth.domain.api.delete_account.operation.DeleteAccount.Error.InvalidCredentials
import com.book.auth.domain.datasource.AccountDataSource
import com.book.core.data.eventstreaming.StandardEventProducer
import com.book.core.data.eventstreaming.send
import com.book.user.domain.api.event.UserEvents.DeleteUserEvent

internal class DeleteAccountImpl(
    private val finishAssertion: FinishAssertion,
    private val accountDataSource: AccountDataSource,
    private val eventProducer: StandardEventProducer
) : DeleteAccount {

    override suspend fun invoke(userId: Long, request: FinishAssertionRequest): Result<Unit> = runCatching {
        finishAssertion(request).getOrThrow()
        val authRecord = accountDataSource.getAuthRecordByUserId(userId) ?: throw InvalidCredentials
        eventProducer.send(DeleteUserEvent(authRecord.userId))
        accountDataSource.deleteAuthorization(authRecord.userId)
    }

}