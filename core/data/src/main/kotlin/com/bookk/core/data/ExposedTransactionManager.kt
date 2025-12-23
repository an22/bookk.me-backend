package com.bookk.core.data


import com.bookk.core.data.map.toDomain
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.entity.BusinessError
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

class ExposedTransactionManager : TransactionManager {
    override suspend fun <T> transaction(transaction: suspend () -> T): Result<T> {
        return runCatching {
            suspendTransaction {
                transaction()
            }
        }.recoverCatching {
            throw when(it) {
                is BusinessError -> it
                else -> it.toDomain()
            }
        }
    }
}