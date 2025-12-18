package com.bookk.auth.domain.impl.operation

import com.bookk.auth.domain.api.authentication.entity.FinishAssertionRequest
import com.bookk.auth.domain.api.authentication.operation.FinishAssertion
import com.bookk.auth.domain.api.delete_account.operation.DeleteAccount
import com.bookk.auth.domain.api.delete_account.operation.DeleteAccount.Error.InvalidCredentials
import com.bookk.auth.domain.datasource.AccountDataSource
import com.bookk.core.data.eventstreaming.StandardEventProducer
import com.bookk.core.data.eventstreaming.send
import com.bookk.server.business.client.api.event.BusinessEvent.DeleteBusinessesForUserEvent
import com.bookk.server.user.client.api.event.UserEvents.DeleteUserEvent
import kotlin.uuid.Uuid

internal class DeleteAccountImpl(
    private val finishAssertion: FinishAssertion,
    private val accountDataSource: AccountDataSource,
    private val eventProducer: StandardEventProducer
) : DeleteAccount {

    override suspend fun invoke(userId: Uuid, request: FinishAssertionRequest): Result<Unit> = runCatching {
        finishAssertion(request).getOrThrow()
        val authRecord = accountDataSource.getAuthRecordByUserId(userId) ?: throw InvalidCredentials
        eventProducer.send(DeleteUserEvent(authRecord.userId))
        eventProducer.send(DeleteBusinessesForUserEvent(authRecord.userId))
        accountDataSource.deleteAuthorization(authRecord.userId)
    }

}