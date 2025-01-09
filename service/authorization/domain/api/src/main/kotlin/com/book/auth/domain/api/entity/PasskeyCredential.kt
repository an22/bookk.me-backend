package com.book.auth.domain.api.entity

class PasskeyCredential(
    val id: Long,
    val authId: Long,
    val userHandle: ByteArray,
    val credDescriptorId: ByteArray,
    val credDescriptorType: String,
    val credDescriptorTransports: String,
    val publicKey: String,
    val signatureCount: Long,
    val isDiscoverable: Boolean,
    val isBackupEligible: Boolean,
    val isBackedUp: Boolean,
    val attestationObject: ByteArray,
    val clientData: ByteArray
)