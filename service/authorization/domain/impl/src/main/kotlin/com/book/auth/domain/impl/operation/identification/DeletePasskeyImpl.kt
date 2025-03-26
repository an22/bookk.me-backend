package com.book.auth.domain.impl.operation.identification

import com.book.auth.domain.api.identification.operation.DeletePasskey
import com.book.auth.domain.datasource.PassKeyDataSource

internal class DeletePasskeyImpl(
    private val passKeyDataSource: PassKeyDataSource
) : DeletePasskey {
    override suspend fun invoke(id: Long): Result<Unit> = runCatching {
        passKeyDataSource.deletePasskey(id)
    }
}