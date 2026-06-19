package com.bookk.auth.domain.api.token.operation

interface RotateSigningKeys {
    suspend operator fun invoke(): Result<Unit>
}
