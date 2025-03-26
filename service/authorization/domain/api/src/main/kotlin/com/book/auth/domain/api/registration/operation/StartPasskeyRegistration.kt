package com.book.auth.domain.api.registration.operation

import com.book.auth.domain.api.registration.entity.RegistrationChallengeResponse

interface StartPasskeyRegistration {
    suspend operator fun invoke(userHandle: ByteArray, passkeyDisplayName: String): Result<RegistrationChallengeResponse>
}