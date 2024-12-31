@file:OptIn(ExperimentalEncodingApi::class)

package com.book.core.service.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.RSAKeyProvider
import com.bookk.core.AppLevelConstants
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTCredential
import kotlinx.datetime.Clock
import java.nio.charset.Charset
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


@OptIn(ExperimentalEncodingApi::class)
object JwtConfig {
    private const val ISSUER = "com.bookk.server"

    val verifier: JWTVerifier = JWT
        .require(Algorithm.RSA256(createPublicKeyProvider()))
        .withIssuer(ISSUER)
        .withAudience(AppLevelConstants.DOMAIN_NAME)
        .build()

    val validator: ApplicationCall.(JWTCredential) -> AppPrincipal? = { credentials ->
        val isNotExpired = credentials.payload.expiresAt.time > Clock.System.now().toEpochMilliseconds()
        val isRefresh = credentials.payload.getClaim(Claim.REFRESH.key).asBoolean()
        if (isNotExpired && !isRefresh) {
            AppPrincipal(
                userId = credentials.payload.getClaim(Claim.ID.key).asLong(),
                userName = credentials.payload.getClaim(Claim.USERNAME.key).asString(),
                role = credentials.payload.getClaim(Claim.ROLE.key).asInt(),
                deviceId = credentials.payload.getClaim(Claim.DEVICE_ID.key).asLong()
            )
        }
        else null
    }

    private fun createPublicKeyProvider(): RSAKeyProvider {
        val store = KeyFactory.getInstance("RSA")
        val public = store.generatePublic(X509EncodedKeySpec(readPublicPemFile()))
        return object : RSAKeyProvider {
            override fun getPublicKeyById(id: String?): RSAPublicKey = public as RSAPublicKey

            override fun getPrivateKey(): RSAPrivateKey? = null

            override fun getPrivateKeyId(): String? = null

        }
    }

    fun createPrivateKeyProvider(): RSAKeyProvider {
        val store = KeyFactory.getInstance("RSA")
        val public = store.generatePublic(X509EncodedKeySpec(readPublicPemFile()))
        val private = store.generatePrivate(PKCS8EncodedKeySpec(readPrivatePemFile()))
        return object : RSAKeyProvider {
            override fun getPublicKeyById(id: String?): RSAPublicKey = public as RSAPublicKey

            override fun getPrivateKey(): RSAPrivateKey = private as RSAPrivateKey

            override fun getPrivateKeyId(): String? = null

        }
    }

    private fun readPublicPemFile(): ByteArray {
        return javaClass.classLoader
            .getResource(System.getenv("BOOKK_ME_JWT_PUBLIC_KEY_FILE"))!!
            .readBytes()
            .toString(Charset.defaultCharset())
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace(System.lineSeparator(), "")
            .replace("-----END PUBLIC KEY-----", "")
            .let { Base64.decode(it) }
    }

    private fun readPrivatePemFile(): ByteArray {
        return javaClass.classLoader
            .getResource(System.getenv("BOOKK_ME_JWT_PRIVATE_KEY_FILE"))!!
            .readBytes()
            .toString(Charset.defaultCharset())
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace(System.lineSeparator(), "")
            .replace("-----END PRIVATE KEY-----", "")
            .let { Base64.decode(it) }
    }
}