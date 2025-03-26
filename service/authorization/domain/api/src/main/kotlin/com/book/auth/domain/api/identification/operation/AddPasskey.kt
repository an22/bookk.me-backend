package com.book.auth.domain.api.identification.operation

import com.book.auth.domain.api.identification.entity.AddPasskeyRequest

interface AddPasskey {
    suspend operator fun invoke(request: AddPasskeyRequest): Result<Unit>
}