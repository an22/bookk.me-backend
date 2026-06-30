package com.bookk.business.domain.impl.operation.service

import com.bookk.business.domain.api.service.entity.Quote
import com.bookk.business.domain.api.service.entity.Service
import com.bookk.business.domain.api.service.operation.IssueQuote
import com.bookk.business.domain.datasource.ServiceDataSource
import com.bookk.core.AppLevelConstants
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.server.business.client.api.QuoteClaims
import library.signing.TokenIssuer
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.Uuid

internal class IssueQuoteImpl(
    private val serviceDataSource: ServiceDataSource,
    private val transactionManager: TransactionManager,
    private val tokenIssuer: TokenIssuer
) : IssueQuote {

    override suspend fun invoke(businessId: Uuid, serviceIds: List<Uuid>): Result<Quote> {
        if (serviceIds.isEmpty()) return Result.failure(IssueQuote.Error.EmptyServiceList())
        return transactionManager.transaction {
            val services = serviceDataSource.getServicesByIds(serviceIds)
            if (services.size != serviceIds.size) throw IssueQuote.Error.ServiceNotFound()

            val quoteId = Uuid.random()
            val token = buildToken(quoteId, businessId, services)

            Quote(id = quoteId, services = services, token = token)
        }
    }

    private suspend fun buildToken(quoteId: Uuid, businessId: Uuid, services: List<Service>): String {
        val total = services.map { it.price }.reduce { acc, price -> acc + price }
        return tokenIssuer.issue(QUOTE_TTL) {
            withJWTId(quoteId.toString())
            .withAudience(AppLevelConstants.domainName)
            .withClaim(QuoteClaims.CLAIM_BUSINESS_ID, businessId.toString())
            .withClaim(QuoteClaims.CLAIM_SERVICES, services.map { it.id.toString() })
            .withClaim(QuoteClaims.CLAIM_TOTAL, total.toString())
        }
    }

    companion object {
        private val QUOTE_TTL = 10.minutes
    }
}
