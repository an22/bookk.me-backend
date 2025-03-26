package com.book.auth.domain.api.authentication.entity

interface FinishRegistrationRequest {
    val requestId: String
    val publicKeyCredentialJson: String
}