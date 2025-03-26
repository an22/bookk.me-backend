package com.book.auth.domain.api.registration.operation

import com.book.auth.domain.api.identification.entity.AddPasskeyRequest

interface AttachNewPasskeyToAccount {
    suspend operator fun invoke(authId: Long, request: AddPasskeyRequest): Result<Unit>
}