package com.book.auth.domain.api.registration.operation

import com.book.auth.domain.api.registration.entity.RegistrationChallengeResponse
import kotlin.uuid.Uuid

interface StartPasskeyRegistration {
    suspend operator fun invoke(userHandle: Uuid, passkeyDisplayName: String): Result<RegistrationChallengeResponse>
}