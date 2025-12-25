package com.bookk.auth.domain.api.authentication.entity

import kotlinx.serialization.Serializable

@Serializable
class EditEmailRequest(
    override val requestId: String,
    override val publicKeyCredentialJson: String,
    val newEmail: String
) : FinishAssertionRequest