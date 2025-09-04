package com.book.core.data


import com.book.core.domain.datasource.transaction.TransactionManager
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction

class ExposedTransactionManager: TransactionManager {
    override suspend fun <T> runInTransaction(transaction: suspend () -> T): Result<T> {
        return runCatching {
            suspendTransaction {
                transaction()
            }
        }
    }
}