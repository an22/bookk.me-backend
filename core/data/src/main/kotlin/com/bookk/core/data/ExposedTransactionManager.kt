package com.bookk.core.data


import com.bookk.core.data.map.toDomain
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.entity.BusinessError
import com.bookk.core.domain.entity.Error
import com.bookk.core.domain.entity.runSuspendCatching
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

class ExposedTransactionManager(private val database: Database) : TransactionManager {
    override suspend fun <T> transaction(transaction: suspend () -> T): Result<T> {
        return runSuspendCatching {
            suspendTransaction(db = database) {
                transaction()
            }
        }.recoverCatching {
            throw when (it) {
                is BusinessError -> it
                is Error -> it
                else -> it.toDomain()
            }
        }
    }
}