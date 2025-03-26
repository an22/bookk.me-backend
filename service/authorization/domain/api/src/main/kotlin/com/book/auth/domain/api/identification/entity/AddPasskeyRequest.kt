package com.book.auth.domain.api.identification.entity

import com.book.auth.domain.api.authentication.entity.FinishRegistrationRequest
import kotlinx.serialization.Serializable

@Serializable
class AddPasskeyRequest(
    override val requestId: String,
    override val publicKeyCredentialJson: String
) : FinishRegistrationRequest