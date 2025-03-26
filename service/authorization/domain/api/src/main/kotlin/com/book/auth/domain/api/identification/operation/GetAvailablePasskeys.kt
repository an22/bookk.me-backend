package com.book.auth.domain.api.identification.operation

import com.book.auth.domain.api.identification.entity.PasskeyResponse

interface GetAvailablePasskeys {
    suspend operator fun invoke(authId: Long): Result<List<PasskeyResponse>>
}