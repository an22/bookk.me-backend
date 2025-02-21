@file:OptIn(ExperimentalEncodingApi::class)

package com.book.core.service.auth

import com.auth0.jwt.interfaces.RSAKeyProvider
import java.io.File
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

    fun createPublicKeyProvider(): RSAKeyProvider {
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
        return runCatching {
            File(System.getenv("BOOKK_ME_JWT_PUBLIC_KEY_FILE"))
                .readBytes()
        }.getOrElse {
            javaClass.classLoader
                .getResource(System.getenv("BOOKK_ME_JWT_PUBLIC_KEY_FILE"))!!
                .readBytes()
        }.toString(Charset.defaultCharset())
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace(System.lineSeparator(), "")
            .replace("-----END PUBLIC KEY-----", "")
            .let { Base64.decode(it) }
    }

    private fun readPrivatePemFile(): ByteArray {
        return runCatching {
            File(System.getenv("BOOKK_ME_JWT_PRIVATE_KEY_FILE"))
                .readBytes()
        }.getOrElse {
            javaClass.classLoader
                .getResource(System.getenv("BOOKK_ME_JWT_PRIVATE_KEY_FILE"))!!
                .readBytes()
        }.toString(Charset.defaultCharset())
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace(System.lineSeparator(), "")
            .replace("-----END PRIVATE KEY-----", "")
            .let { Base64.decode(it) }
    }
}