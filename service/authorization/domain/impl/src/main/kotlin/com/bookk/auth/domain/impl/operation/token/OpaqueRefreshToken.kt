package com.bookk.auth.domain.impl.operation.token

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import kotlin.uuid.Uuid

internal object OpaqueRefreshToken {
    private const val SECRET_BYTES = 32
    private val secureRandom = SecureRandom()

    class Generated(val id: Uuid, val secretHash: String, val token: String)

    fun generate(): Generated {
        val id = Uuid.random()
        val secret = randomSecret()
        return Generated(id, hash(secret), "$id.$secret")
    }

    fun parse(token: String): Pair<Uuid, String>? {
        val separatorIndex = token.indexOf('.')
        if (separatorIndex <= 0 || separatorIndex == token.lastIndex) return null
        val id = runCatching { Uuid.parse(token.substring(0, separatorIndex)) }.getOrNull() ?: return null
        return id to token.substring(separatorIndex + 1)
    }

    fun matches(secret: String, expectedHash: String): Boolean {
        return MessageDigest.isEqual(hash(secret).toByteArray(), expectedHash.toByteArray())
    }

    private fun randomSecret(): String {
        val bytes = ByteArray(SECRET_BYTES).also(secureRandom::nextBytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private fun hash(secret: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(secret.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
