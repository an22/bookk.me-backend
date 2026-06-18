package com.bookk.auth.domain.api.token.operation

import com.bookk.auth.domain.api.token.entity.SigningKey

interface GetActiveSigningKey {
    suspend operator fun invoke(): Result<SigningKey>
}
