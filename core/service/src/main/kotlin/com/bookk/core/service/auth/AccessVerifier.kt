package com.bookk.core.service.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.bookk.core.AppLevelConstants
import com.bookk.core.AppLevelConstants.Claim
import com.bookk.core.service.auth.JwtConfig.createJwksKeyProvider
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTCredential
import kotlin.time.Clock
import kotlin.uuid.Uuid

object AccessVerifier {

    private val issuer: String = "https://${AppLevelConstants.domainName}"

    val verifier: JWTVerifier = JWT
        .require(Algorithm.RSA256(createJwksKeyProvider()))
        .withIssuer(issuer)
        .withAudience(issuer)
        .build()

    val validator: ApplicationCall.(JWTCredential) -> AppPrincipal? = { credentials ->
        val isNotExpired = credentials.payload.expiresAt.time > Clock.System.now().toEpochMilliseconds()
        if (isNotExpired) {
            AppPrincipal(
                authId = Uuid.parse(credentials.payload.getClaim(Claim.AUTH_ID.key).asString()),
                userId = Uuid.parse(credentials.payload.getClaim(Claim.USER_ID.key).asString()),
                deviceId = Uuid.parse(credentials.payload.getClaim(Claim.DEVICE_ID.key).asString())
            )
        } else null
    }
}