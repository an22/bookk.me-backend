package com.bookk.auth.domain.impl.operation.token

import com.bookk.auth.domain.api.token.entity.SigningKeyStatus
import com.bookk.auth.domain.api.token.operation.RotateSigningKeys
import com.bookk.auth.domain.datasource.SigningKeyDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

internal class RotateSigningKeysImpl(
    private val signingKeyDataSource: SigningKeyDataSource,
    private val transactionManager: TransactionManager
) : RotateSigningKeys {

    override suspend fun invoke(): Result<Unit> = transactionManager.transaction {
        val previousActive = signingKeyDataSource.getActiveKey()
        val (publicKeyPem, privateKeyPem) = RsaSigningKeyFactory.generate()
        signingKeyDataSource.insertKey(publicKeyPem, privateKeyPem)

        if (previousActive != null) {
            signingKeyDataSource.updateStatus(previousActive.id, SigningKeyStatus.RETIRING, Clock.System.now())
        }

        signingKeyDataSource.deleteRetiredBefore(Clock.System.now().minus(RETIRING_RETENTION))
    }

    companion object {
        private val RETIRING_RETENTION = 8.days
    }
}
