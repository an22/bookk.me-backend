package com.book.auth.domain.impl.operation

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.RSAKeyProvider
import com.book.auth.domain.api.datasource.UserAuthDataSource
import com.book.auth.domain.api.entity.DeviceAuthRecord
import com.book.auth.domain.api.entity.TokenInfo
import com.book.auth.domain.api.operation.GenerateAuthToken
import com.book.auth.domain.api.operation.GenerateAuthToken.GenerateAuthTokenBusinessError.InvalidCredentials
import com.book.auth.domain.api.operation.GenerateAuthToken.Param
import com.book.auth.domain.impl.totp.createTotpConfig
import com.book.user.domain.api.util.createPasswordHash
import dev.turingcomplete.kotlinonetimepassword.TimeBasedOneTimePasswordGenerator
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import org.apache.commons.codec.binary.Base32
import kotlin.time.Duration.Companion.milliseconds

internal class GenerateAuthTokenImpl(
    private val serviceUrl: String,
    private val keyProvider: RSAKeyProvider,
    private val localDataSource: UserAuthDataSource
) : GenerateAuthToken {
    private val totpConfig = createTotpConfig()
    private val base32 = Base32()

    override suspend fun call(params: Param): Result<TokenInfo> = runCatching {
        val deviceRecord = params.getAuthRecord() ?: throw InvalidCredentials
        val accessToken = createToken(deviceRecord, ACCESS_EXPIRATION_TIME, false)
        val refreshToken = createToken(deviceRecord, REFRESH_EXPIRATION_TIME, true)
        localDataSource.saveUserRefreshToken(deviceRecord.deviceInfo.id, refreshToken)
        TokenInfo(accessToken, refreshToken)
    }

    private fun createToken(record: DeviceAuthRecord, expirationMs: Long, isRefresh: Boolean): String {
        return JWT.create()
            .withAudience(serviceUrl)
            .withIssuer(ISSUER)
            .withClaim("refresh", isRefresh)
            .withClaim("username", record.authRecord.login)
            .withClaim("id", record.authRecord.userId)
            .withClaim("role", record.authRecord.role.id)
            .withClaim("device_id", record.deviceInfo.id)
            .withIssuedAt(Clock.System.now().toJavaInstant())
            .withNotBefore(Clock.System.now().toJavaInstant())
            .withExpiresAt(Clock.System.now().plus(expirationMs.milliseconds).toJavaInstant())
            .sign(Algorithm.RSA256(keyProvider))
    }

    private suspend fun Param.getAuthRecord(): DeviceAuthRecord? {
        return when (this) {
            is Param.FromCredentials -> {
                val authRecord = localDataSource.getAuthRecordByUsername(info.login) ?: return null
                val generator = TimeBasedOneTimePasswordGenerator(
                    secret = base32.decode(authRecord.totpSecret),
                    config = totpConfig
                )
                if (!generator.isValid(info.totpCode)) return null
                val device = localDataSource.getDevice(authRecord.id, info.deviceName)
                if (device == null) localDataSource.createDevice(authRecord.id, info.deviceName)
                localDataSource.getDeviceAuthRecord(info.deviceName, info.login, createPasswordHash(info.password))
            }

            is Param.FromRefresh -> localDataSource.getDeviceAuthRecord(userId, refreshToken)
        }
    }

    companion object {
        private const val ACCESS_EXPIRATION_TIME = 1000L * 60 * 60
        private const val REFRESH_EXPIRATION_TIME = ACCESS_EXPIRATION_TIME * 24
        private const val ISSUER = "com.bookk.server"
    }
}