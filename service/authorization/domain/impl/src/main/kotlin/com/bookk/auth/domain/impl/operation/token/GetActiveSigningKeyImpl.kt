package com.bookk.auth.domain.impl.operation.token

import com.bookk.auth.domain.api.token.entity.SigningKey
import com.bookk.auth.domain.api.token.operation.GetActiveSigningKey
import com.bookk.auth.domain.datasource.SigningKeyDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager

internal class GetActiveSigningKeyImpl(
    private val signingKeyDataSource: SigningKeyDataSource,
    private val transactionManager: TransactionManager
) : GetActiveSigningKey {

    override suspend fun invoke(): Result<SigningKey> = transactionManager.transaction {
        signingKeyDataSource.getActiveKey() ?: run {
            val (publicKeyPem, privateKeyPem) = RsaSigningKeyFactory.generate()
            signingKeyDataSource.insertKey(publicKeyPem, privateKeyPem)
        }
    }
}
