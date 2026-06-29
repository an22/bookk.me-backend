package library.signing

import kotlin.time.Instant
import kotlin.uuid.Uuid

class SigningKey(
    val id: Uuid,
    val publicKeyPem: String,
    val privateKeyPem: String,
    val status: SigningKeyStatus,
    val createdAt: Instant,
    val retiredAt: Instant?
)
