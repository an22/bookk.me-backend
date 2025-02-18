package com.book.auth.domain.api.authentication.entity

interface FinishAssertionRequest {
    val requestId: String
    val publicKeyCredentialJson: String
}