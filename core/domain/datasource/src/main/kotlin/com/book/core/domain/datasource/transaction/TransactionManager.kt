package com.book.core.domain.transaction

interface TransactionManager {
    suspend fun <T> runInTransaction(transaction: suspend () -> T): Result<T>
}