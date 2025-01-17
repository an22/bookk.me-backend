package com.book.core.data


import com.book.core.domain.transaction.TransactionManager
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class ExposedTransactionManager: TransactionManager {
    override suspend fun <T> runInTransaction(transaction: suspend () -> T): Result<T> {
        return runCatching {
            newSuspendedTransaction {
                db.config.defaultSchema?.let { SchemaUtils.setSchema(it) }
                transaction()
            }
        }
    }
}