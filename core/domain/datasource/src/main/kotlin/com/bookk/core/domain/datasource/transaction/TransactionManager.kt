package com.bookk.core.domain.datasource.transaction

interface TransactionManager {
    suspend fun <T> transaction(transaction: suspend () -> T): Result<T>
}