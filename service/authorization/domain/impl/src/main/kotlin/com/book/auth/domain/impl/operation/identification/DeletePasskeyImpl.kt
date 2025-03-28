package com.book.auth.domain.impl.operation.identification

import com.book.auth.domain.api.identification.operation.DeletePasskey
import com.book.auth.domain.datasource.PassKeyDataSource

internal class DeletePasskeyImpl(
    private val passKeyDataSource: PassKeyDataSource
) : DeletePasskey {
    override suspend fun invoke(id: Long, authId: Long): Result<Unit> = runCatching {
        if (passKeyDataSource.deletePasskey(id, authId) == 0) {
            throw DeletePasskey.Error.LastPasskey
        }
    }
}