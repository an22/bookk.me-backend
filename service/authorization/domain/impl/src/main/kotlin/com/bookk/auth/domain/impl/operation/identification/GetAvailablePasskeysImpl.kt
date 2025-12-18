package com.bookk.auth.domain.impl.operation.identification

import com.bookk.auth.domain.api.identification.entity.PasskeyResponse
import com.bookk.auth.domain.api.identification.operation.GetAvailablePasskeys
import com.bookk.auth.domain.datasource.PassKeyDataSource
import kotlin.uuid.Uuid

internal class GetAvailablePasskeysImpl(
    private val passKeyDataSource: PassKeyDataSource
) : GetAvailablePasskeys {
    override suspend fun invoke(authId: Uuid): Result<List<PasskeyResponse>> = runCatching {
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