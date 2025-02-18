package com.book.core.service.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.book.core.service.auth.JwtConfig.createPublicKeyProvider
import com.bookk.core.AppLevelConstants
import com.bookk.core.AppLevelConstants.Claim
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTCredential
import kotlinx.datetime.Clock

object AccessVerifier {

    private const val ISSUER = "com.bookk.server"

    val verifier: JWTVerifier = JWT
        .require(Algorithm.RSA256(createPublicKeyProvider()))
        .withIssuer(ISSUER)
        .withAudience(AppLevelConstants.DOMAIN_NAME)
        .build()

    val validator: ApplicationCall.(JWTCredential) -> AppPrincipal? = { credentials ->
        val isNotExpired = credentials.payload.expiresAt.time > Clock.System.now().toEpochMilliseconds()
        if (isNotExpired) {
            AppPrincipal(
                authId = credentials.payload.getClaim(Claim.AUTH_ID.key).asLong(),
                userId = credentials.payload.getClaim(Claim.USER_ID.key).asLong(),
                deviceId = credentials.payload.getClaim(Claim.DEVICE_ID.key).asLong()
            )
        } else null
    }
}