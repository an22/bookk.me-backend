package com.book.auth.domain.api.identification.entity

import com.book.auth.domain.api.authentication.entity.Authentication
import kotlin.time.Instant
import kotlin.uuid.Uuid

/**
 * Entity is immutable and byte arrays expected to never be touched
 * so saving memory by avoiding deep copying arrays is desired behaviour
 */
@Suppress("ArrayInDataClass")
data class PasskeyCredential(
    val id: Uuid,
    val authId: Uuid,
    val authInfo: Authentication,
    val handle: Uuid,
    val name: String,
    val credDescriptor: CredentialDescriptor,
    val publicKey: String,
    val signatureCount: Long,
    val isDiscoverable: Boolean,
    val isBackupEligible: Boolean,
    val isBackedUp: Boolean,
    val attestationObject: ByteArray,
    val clientData: String,
    val createdAt: Instant,
    val lastUsedAt: Instant
) {

    /**
     * @see <a href="https://www.w3.org/TR/webauthn-2/#dictionary-credential-descriptor"</a>
     * */
    class CredentialDescriptor(
        val id: ByteArray,
        val type: String,
        val transports: Set<String>
    )
}