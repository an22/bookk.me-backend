package com.bookk.auth.domain.impl.operation.identification

import com.bookk.auth.domain.api.identification.operation.DeletePasskey
import com.bookk.auth.domain.datasource.PassKeyDataSource
import kotlin.uuid.Uuid

internal class DeletePasskeyImpl(
    private val passKeyDataSource: PassKeyDataSource
) : DeletePasskey {
    override suspend fun invoke(id: Uuid, authId: Uuid): Result<Unit> = runCatching {
        if (passKeyDataSource.deletePasskey(id, authId) == 0) {
            throw DeletePasskey.Error.LastPasskey
        }
    }
}