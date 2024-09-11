package com.book.user.domain.impl.operation

import com.book.user.domain.api.datasource.UserLocalDataSource
import com.book.user.domain.api.operation.IsUserExistWithParameters

internal class IsUserExistWithParametersImpl(
    private val localDataSource: UserLocalDataSource
) : IsUserExistWithParameters {
    override suspend fun call(params: IsUserExistWithParameters.Param): Result<Boolean> {
        return runCatching {
            localDataSource.getUserByPhoneOrEmail(params.phone, params.email) != null
        }
    }
}