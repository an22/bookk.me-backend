package com.book.auth.domain.impl.operation.registration

import com.book.auth.domain.api.identification.entity.AddPasskeyRequest
import com.book.auth.domain.api.registration.operation.AttachNewPasskeyToAccount
import com.book.auth.domain.api.registration.operation.FinishPasskeyRegistration
import kotlin.uuid.Uuid

internal class AttachNewPasskeyToAccountImpl(
    private val finishPasskeyRegistration: FinishPasskeyRegistration
) : AttachNewPasskeyToAccount {
    override suspend fun invoke(authId: Uuid, request: AddPasskeyRequest): Result<Unit> = runCatching {
        val passkey = finishPasskeyRegistration.verifyRequest(request).getOrThrow()
        return finishPasskeyRegistration.attachOwner(authId, passkey)
    }
}