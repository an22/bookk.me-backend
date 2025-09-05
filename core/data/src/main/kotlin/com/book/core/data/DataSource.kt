package com.book.core.data

import com.book.core.data.map.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.withTimeout
import org.jetbrains.exposed.v1.r2dbc.R2dbcTransaction
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction

enum class QueryType(val limit: Long) {
    FAST(10000),
    GENERIC(60000),
    BACKGROUND(5 * 60000)
}

abstract class DataSource {

    suspend fun <T> mapExceptions(
        finally: () -> Unit = {},
        action: suspend () -> T
    ): T {
        return try {
            action()
        } catch (e: Exception) {
            throw e.toDomain()
        } finally {
            finally()
        }
    }

    suspend fun <T> dbQuery(
        type: QueryType = QueryType.FAST,
        block: suspend R2dbcTransaction.() -> T
    ): T = withTimeout(timeMillis = type.limit) {
        suspendTransaction {
            block()
        }
    }

    fun <T> Flow<T>.handleErrors(): Flow<T> {
        return catch { e ->
            throw e.toDomain()
        }
    }
}