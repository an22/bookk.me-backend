package com.bookk.auth.domain.impl.operation.token

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.RSAKeyProvider
import com.bookk.auth.domain.api.identification.entity.Device
import com.bookk.auth.domain.api.token.entity.AuthTokens
import com.bookk.auth.domain.api.token.operation.GenerateAuthToken
import com.bookk.auth.domain.api.token.operation.GenerateAuthToken.Error.InvalidCredentials
import com.bookk.auth.domain.api.token.operation.GenerateAuthToken.Source
import com.bookk.auth.domain.datasource.DeviceDataSource
import com.bookk.core.AppLevelConstants.Claim
import com.bookk.core.domain.datasource.transaction.TransactionManager
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.toJavaInstant
import kotlin.uuid.Uuid

internal class GenerateAuthTokenImpl(
    private val serviceUrl: String,
    private val keyProvider: RSAKeyProvider,
    private val deviceDataSource: DeviceDataSource,
    private val transactionManager: TransactionManager
) : GenerateAuthToken {

    override suspend fun invoke(source: Source): Result<AuthTokens> = transactionManager.transaction {
        val deviceRecord = source.getDevice() ?: throw InvalidCredentials
        val accessToken = createAccessToken(deviceRecord)
        val refreshId = Uuid.random()
        val refreshToken = createRefreshToken(refreshId, deviceRecord)
        deviceDataSource.attachRefreshTokenToDevice(deviceRecord.deviceInfo.id, refreshId)
        AuthTokens(accessToken, refreshToken)
    }

    private fun createAccessToken(record: Device): String {
        return JWT.create()
            .withAudience(serviceUrl)
            .withIssuer(ISSUER)
            .withJWTId(Uuid.random().toString())
            .withClaim(Claim.AUTH_ID.key, record.authRecord.id.toString())
            .withClaim(Claim.USER_ID.key, record.authRecord.userId.toString())
            .withClaim(Claim.DEVICE_ID.key, record.deviceInfo.id.toString())
            .withIssuedAt(Clock.System.now().toJavaInstant())
            .withNotBefore(Clock.System.now().toJavaInstant())
            .withExpiresAt(Clock.System.now().plus(ACCESS_EXPIRATION_TIME.milliseconds).toJavaInstant())
            .sign(Algorithm.RSA256(keyProvider))
    }

    private fun createRefreshToken(tokenId: Uuid, record: Device): String {
        return JWT.create()
            .withAudience(serviceUrl)
            .withIssuer(REFRESH_ISSUER)
            .withJWTId(tokenId.toString())
            .withClaim(Claim.DEVICE_ID.key, record.deviceInfo.id.toString())
            .withIssuedAt(Clock.System.now().toJavaInstant())
            .withNotBefore(Clock.System.now().toJavaInstant())
            .withExpiresAt(Clock.System.now().plus(REFRESH_EXPIRATION_TIME.milliseconds).toJavaInstant())
            .sign(Algorithm.RSA256(keyProvider))
    }

    private suspend fun Source.getDevice(): Device? {
        return when (this) {
            is Source.FromRefresh -> deviceDataSource.getDeviceById(deviceId)?.also {
                if (it.deviceInfo.refreshTokenId != tokenId) throw InvalidCredentials
            }

            is Source.FromAuthDevice -> deviceDataSource.getDeviceByAuthIdAndUUID(authId, deviceUUID)
        }
    }

    companion object {
        private const val ACCESS_EXPIRATION_TIME = 1000L * 60 * 5 // 5 Minutes
        private const val REFRESH_EXPIRATION_TIME = 1000L * 60 * 60 * 24 * 7 // 1 Week
        private const val ISSUER = "com.bookk.server"
        private const val REFRESH_ISSUER = "com.bookk.server.refresh"
    }
}