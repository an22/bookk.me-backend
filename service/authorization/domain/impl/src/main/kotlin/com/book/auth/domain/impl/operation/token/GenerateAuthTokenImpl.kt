package com.book.auth.domain.impl.operation.token

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.RSAKeyProvider
import com.book.auth.domain.api.identification.entity.Device
import com.book.auth.domain.api.token.entity.AuthTokens
import com.book.auth.domain.api.token.operation.GenerateAuthToken
import com.book.auth.domain.api.token.operation.GenerateAuthToken.Error.InvalidCredentials
import com.book.auth.domain.api.token.operation.GenerateAuthToken.Source
import com.book.auth.domain.datasource.DeviceDataSource
import com.bookk.core.AppLevelConstants.Claim
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import kotlin.time.Duration.Companion.milliseconds

internal class GenerateAuthTokenImpl(
    private val serviceUrl: String,
    private val keyProvider: RSAKeyProvider,
    private val deviceDataSource: DeviceDataSource,
) : GenerateAuthToken {

    override suspend fun invoke(params: Source): Result<AuthTokens> = runCatching {
        val deviceRecord = params.getDevice() ?: throw InvalidCredentials
        val accessToken = createToken(deviceRecord, ACCESS_EXPIRATION_TIME, false)
        val refreshToken = createToken(deviceRecord, REFRESH_EXPIRATION_TIME, true)
        deviceDataSource.attachRefreshTokenToDevice(deviceRecord.deviceInfo.id, refreshToken)
        AuthTokens(accessToken, refreshToken)
    }

    private fun createToken(record: Device, expirationMs: Long, isRefresh: Boolean): String {
        return JWT.create()
            .withAudience(serviceUrl)
            .withIssuer(ISSUER)
            .withClaim(Claim.IS_REFRESH.key, isRefresh)
            .withClaim(Claim.AUTH_ID.key, record.authRecord.id)
            .withClaim(Claim.USER_ID.key, record.authRecord.userId)
            .withClaim(Claim.DEVICE_ID.key, record.deviceInfo.id)
            .withIssuedAt(Clock.System.now().toJavaInstant())
            .withNotBefore(Clock.System.now().toJavaInstant())
            .withExpiresAt(Clock.System.now().plus(expirationMs.milliseconds).toJavaInstant())
            .sign(Algorithm.RSA256(keyProvider))
    }

    private suspend fun Source.getDevice(): Device? {
        return when (this) {
            is Source.FromRefresh -> JWT.decode(refreshToken).claims[Claim.DEVICE_ID.key]?.asLong()?.let { deviceId ->
                deviceDataSource.getDeviceById(deviceId)
            }

            is Source.FromAuthDevice -> deviceDataSource.getDeviceByAuthIdAndUUID(authId, deviceUUID)
        }
    }

    companion object {
        private const val ACCESS_EXPIRATION_TIME = 1000L * 60 * 60
        private const val REFRESH_EXPIRATION_TIME = ACCESS_EXPIRATION_TIME * 24
        private const val ISSUER = "com.bookk.server"
    }
}