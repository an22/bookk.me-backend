package com.bookk.business.domain.impl.operation.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.RSAKeyProvider
import com.bookk.business.domain.api.service.entity.Quote
import com.bookk.business.domain.api.service.operation.IssueQuote
import com.bookk.business.domain.datasource.ServiceDataSource
import com.bookk.core.AppLevelConstants
import com.bookk.core.domain.datasource.transaction.TransactionManager
import library.signing.GetActiveSigningKey
import library.signing.SigningKey
import library.signing.impl.key.RsaSigningKeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaInstant
import kotlin.uuid.Uuid

internal class IssueQuoteImpl(
    private val serviceDataSource: ServiceDataSource,
    private val getActiveSigningKey: GetActiveSigningKey,
    private val transactionManager: TransactionManager
) : IssueQuote {

    override suspend fun invoke(businessId: Uuid, serviceIds: List<Uuid>): Result<Quote> {
        return transactionManager.transaction {
            val services = serviceDataSource.getServicesByIds(serviceIds)
            if (services.size != serviceIds.size) throw IssueQuote.Error.ServiceNotFound()

            val signingKey = getActiveSigningKey().getOrThrow()
            val quoteId = Uuid.random()
            val token = buildToken(quoteId, businessId, serviceIds, signingKey)

            Quote(id = quoteId, services = services, token = token)
        }
    }

    private fun buildToken(quoteId: Uuid, businessId: Uuid, serviceIds: List<Uuid>, signingKey: SigningKey): String {
        val keyProvider = object : RSAKeyProvider {
            override fun getPublicKeyById(id: String?): RSAPublicKey =
                RsaSigningKeyFactory.parsePublicKey(signingKey.publicKeyPem)

            override fun getPrivateKey(): RSAPrivateKey =
                RsaSigningKeyFactory.parsePrivateKey(signingKey.privateKeyPem)

            override fun getPrivateKeyId(): String = signingKey.id.toString()
        }
        val now = Clock.System.now()
        return JWT.create()
            .withKeyId(signingKey.id.toString())
            .withJWTId(quoteId.toString())
            .withAudience("appointments")
            .withIssuer("business.${AppLevelConstants.domainName}")
            .withClaim(CLAIM_BUSINESS_ID, businessId.toString())
            .withClaim(CLAIM_SERVICE_IDS, serviceIds.map { it.toString() })
            .withIssuedAt(now.toJavaInstant())
            .withExpiresAt(now.plus(QUOTE_TTL).toJavaInstant())
            .sign(Algorithm.RSA256(keyProvider))
    }

    companion object {
        private val QUOTE_TTL = 10.minutes
        const val CLAIM_BUSINESS_ID = "business_id"
        const val CLAIM_SERVICE_IDS = "service_ids"
    }
}
