package com.book.auth.domain.impl.operation.identification

import com.book.auth.domain.api.authentication.operation.FinishAssertion
import com.book.auth.domain.api.identification.entity.AddPasskeyRequest
import com.book.auth.domain.api.identification.operation.AddPasskey
import com.book.auth.domain.datasource.PassKeyDataSource

internal class AddPasskeyImpl(
    private val finishAssertion: FinishAssertion,
    private val passKeyDataSource: PassKeyDataSource
) : AddPasskey {
    override suspend fun invoke(request: AddPasskeyRequest): Result<Unit> {
        val passkey = finishAssertion(request).getOrThrow()

    }
}