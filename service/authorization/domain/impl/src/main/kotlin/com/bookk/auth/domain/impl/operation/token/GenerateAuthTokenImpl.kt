package com.bookk.auth.domain.impl.operation.token

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.RSAKeyProvider
import com.bookk.auth.domain.api.identification.entity.Device
import com.bookk.auth.domain.api.token.entity.AuthTokens
import com.bookk.auth.domain.api.token.operation.GenerateAuthToken
import com.bookk.auth.domain.api.token.operation.GenerateAuthToken.Error.InvalidCredentials
import com.bookk.auth.domain.api.token.operation.GenerateAuthToken.Source
import com.bookk.auth.domain.api.token.operation.GetActiveSigningKey
import com.bookk.auth.domain.datasource.DeviceDataSource
import com.bookk.core.AppLevelConstants.Claim
import com.bookk.core.domain.datasource.transaction.TransactionManager
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.toJavaInstant
import kotlin.uuid.Uuid

internal class GenerateAuthTokenImpl(
    private val serviceUrl: String,
    private val getActiveSigningKey: GetActiveSigningKey,
    private val deviceDataSource: DeviceDataSource,
    private val transactionManager: TransactionManager
) : GenerateAuthToken {

    override suspend fun invoke(source: Source): Result<AuthTokens> = transactionManager.transaction {
        val deviceRecord = source.getDevice() ?: throw InvalidCredentials()
        val accessToken = createAccessToken(deviceRecord)
        val refreshToken = OpaqueRefreshToken.generate()
        deviceDataSource.attachRefreshTokenToDevice(deviceRecord.deviceInfo.id, refreshToken.id, refreshToken.secretHash)
        AuthTokens(accessToken, refreshToken.token)
    }

    private suspend fun createAccessToken(record: Device): String {
        val signingKey = getActiveSigningKey().getOrThrow()
        val keyProvider = object : RSAKeyProvider {
            override fun getPublicKeyById(id: String?): RSAPublicKey = RsaSigningKeyFactory.parsePublicKey(signingKey.publicKeyPem)
            override fun getPrivateKey(): RSAPrivateKey = RsaSigningKeyFactory.parsePrivateKey(signingKey.privateKeyPem)
            override fun getPrivateKeyId(): String = signingKey.id.toString()
        }
        return JWT.create()
            .withKeyId(signingKey.id.toString())
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

    private suspend fun Source.getDevice(): Device? {
        return when (this) {
            is Source.FromRefresh -> validateRefreshToken(tokenId, secret)
            is Source.FromAuthDevice -> deviceDataSource.getDeviceByAuthIdAndUUID(authId, deviceUUID)
        }
    }

    private suspend fun validateRefreshToken(tokenId: Uuid, secret: String): Device? {
        val device = deviceDataSource.getDeviceByRefreshTokenId(tokenId) ?: return null
        val expectedHash = device.deviceInfo.refreshTokenHash ?: return null
        return device.takeIf { OpaqueRefreshToken.matches(secret, expectedHash) }
    }

    companion object {
        private const val ACCESS_EXPIRATION_TIME = 1000L * 60 * 5 // 5 Minutes
        private const val ISSUER = "com.bookk.server"
    }
}