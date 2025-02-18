package com.book.auth.domain.impl.operation.authentication

import com.book.auth.domain.api.authentication.entity.EditEmailRequest
import com.book.auth.domain.api.authentication.operation.EditEmail
import com.book.auth.domain.api.authentication.operation.EditEmail.Error
import com.book.auth.domain.api.authentication.operation.FinishAssertion
import com.book.auth.domain.datasource.AccountDataSource
import com.book.user.domain.api.entity.UserEditModel
import com.bookk.server.user.client.UserClient

internal class EditEmailImpl(
    private val finishAssertion: FinishAssertion,
    private val accountDataSource: AccountDataSource,
    private val userClient: UserClient
) : EditEmail {
    override suspend fun invoke(authId: Long, request: EditEmailRequest): Result<Unit> = runCatching {
        finishAssertion(request).getOrThrow()
        val recordToChange = accountDataSource.getAuthRecordById(authId) ?: throw Error.UserNotFound
        val emailRecord = accountDataSource.getAuthRecordByEmail(request.newEmail)
        val newEmailIsTaken = emailRecord != null && emailRecord.id != authId
        val emailNotChanged = emailRecord != null && emailRecord.id == authId
        when {
            newEmailIsTaken -> throw Error.EmailTaken
            emailNotChanged -> {}
            else -> {
                accountDataSource.setNewEmail(authId, request.newEmail)
                userClient.editUser(recordToChange.userId, UserEditModel(email = request.newEmail))
            }
        }
    }
}