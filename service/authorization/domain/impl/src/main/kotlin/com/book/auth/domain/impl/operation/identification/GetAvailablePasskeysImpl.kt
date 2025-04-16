package com.book.auth.domain.impl.operation.identification

import com.book.auth.domain.api.identification.entity.PasskeyResponse
import com.book.auth.domain.api.identification.operation.GetAvailablePasskeys
import com.book.auth.domain.datasource.PassKeyDataSource

internal class GetAvailablePasskeysImpl(
    private val passKeyDataSource: PassKeyDataSource
) : GetAvailablePasskeys {
    override suspend fun invoke(authId: Long): Result<List<PasskeyResponse>> = runCatching {
        passKeyDataSource.getCredentialBy(authId)
            .map {
                PasskeyResponse(
                    id = it.id,
                    name = it.name,
                    createdAt = it.createdAt,
                    lastUsedAt = it.lastUsedAt,
                    isBackedUp = it.isBackedUp
                )
            }
    }
}