package com.bookk.auth.domain.api.token.operation

import com.bookk.auth.domain.api.token.entity.SigningKey

interface GetVerificationKeys {
    suspend operator fun invoke(): Result<List<SigningKey>>
}
