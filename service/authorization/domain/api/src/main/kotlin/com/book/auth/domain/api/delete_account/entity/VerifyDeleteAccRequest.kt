package com.book.auth.domain.api.delete_account.entity

import com.book.auth.domain.api.authentication.entity.FinishAssertionRequest
import kotlinx.serialization.Serializable

@Serializable
class VerifyDeleteAccRequest(
    override val requestId: String,
    override val publicKeyCredentialJson: String,
) : FinishAssertionRequest