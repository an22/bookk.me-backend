package com.book.core.domain.datasource.transaction

interface TransactionManager {
    suspend fun <T> runInTransaction(transaction: suspend () -> T): Result<T>
}