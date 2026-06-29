package com.bookk.core.service.auth

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.math.BigInteger
import java.security.KeyFactory
import java.security.interfaces.RSAPublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import kotlin.uuid.Uuid

val jwksJson = Json { encodeDefaults = true }

@Serializable
data class Jwk(
    val kty: String = "RSA",
    val use: String = "sig",
    val alg: String = "RS256",
    val kid: String,
    val n: String,
    val e: String
)

@Serializable
data class JwkSet(val keys: List<Jwk>)

fun createJwk(id: Uuid, publicKeyPem: String): Jwk {
    val publicKey = publicKeyPem.toRsaPublicKey()
    return Jwk(
        kid = id.toString(),
        n = publicKey.modulus.toBase64Url(),
        e = publicKey.publicExponent.toBase64Url()
    )
}

fun String.toRsaPublicKey(): RSAPublicKey {
    val base64 = replace("-----BEGIN PUBLIC KEY-----", "")
        .replace("-----END PUBLIC KEY-----", "")
        .lines()
        .joinToString("") { it.trim() }
    val spec = X509EncodedKeySpec(Base64.getDecoder().decode(base64))
    return KeyFactory.getInstance("RSA").generatePublic(spec) as RSAPublicKey
}

fun BigInteger.toBase64Url(): String {
    val bytes = toByteArray().let { if (it.size > 1 && it[0] == 0.toByte()) it.copyOfRange(1, it.size) else it }
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
}