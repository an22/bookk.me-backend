package com.bookk.core.service.auth

import com.auth0.jwk.JwkProviderBuilder
import com.auth0.jwt.interfaces.RSAKeyProvider
import com.bookk.core.AppLevelConstants
import java.net.URI
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.concurrent.TimeUnit

object JwtConfig {

    fun createJwksKeyProvider(): RSAKeyProvider {
        val jwksUrl = URI("http://${AppLevelConstants.authServiceHostname}/jwks.json").toURL()
        val provider = JwkProviderBuilder(jwksUrl)
            .cached(10, 24, TimeUnit.HOURS)
            .rateLimited(10, 1, TimeUnit.MINUTES)
            .build()

        return object : RSAKeyProvider {
            override fun getPublicKeyById(id: String?): RSAPublicKey {
                val keyId = requireNotNull(id) { "Access token is missing its kid header" }
                return provider.get(keyId).publicKey as RSAPublicKey
            }

            override fun getPrivateKey(): RSAPrivateKey? = null

            override fun getPrivateKeyId(): String? = null
        }
    }
}
