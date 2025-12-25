package com.bookk.core.service.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.bookk.core.AppLevelConstants
import com.bookk.core.AppLevelConstants.Claim
import com.bookk.core.service.auth.JwtConfig.createPublicKeyProvider
import com.bookk.core.toUUID
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTCredential
import kotlin.time.Clock

object RefreshVerifier {

    private const val ISSUER = "com.bookk.server.refresh"

    val verifier: JWTVerifier = JWT
        .require(Algorithm.RSA256(createPublicKeyProvider()))
        .withIssuer(ISSUER)
        .withAudience(AppLevelConstants.DOMAIN_NAME)
        .build()

    val validator: ApplicationCall.(JWTCredential) -> RefreshPrincipal? = { credentials ->
        val isNotExpired = credentials.payload.expiresAt.time > Clock.System.now().toEpochMilliseconds()
        if (isNotExpired) {
            RefreshPrincipal(
                deviceId = credentials.payload.getClaim(Claim.DEVICE_ID.key).asString().toUUID(),
                tokenId = requireNotNull(credentials.jwtId).toUUID()
            )
        } else null
    }
}