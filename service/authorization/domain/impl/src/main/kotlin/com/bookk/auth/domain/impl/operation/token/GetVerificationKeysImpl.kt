package com.bookk.auth.domain.impl.operation.token

import com.bookk.auth.domain.api.token.entity.SigningKey
import com.bookk.auth.domain.api.token.operation.GetVerificationKeys
import com.bookk.auth.domain.datasource.SigningKeyDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager

internal class GetVerificationKeysImpl(
    private val signingKeyDataSource: SigningKeyDataSource,
    private val transactionManager: TransactionManager
) : GetVerificationKeys {

    override suspend fun invoke(): Result<List<SigningKey>> = transactionManager.transaction {
        signingKeyDataSource.getVerificationKeys()
    }
}
