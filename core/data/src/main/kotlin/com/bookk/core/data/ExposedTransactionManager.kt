package com.bookk.core.data


import com.bookk.core.data.map.toDomain
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.entity.BusinessError
import com.bookk.core.domain.entity.Error
import com.bookk.core.domain.entity.runSuspendCatching
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager as JdbcTransactionManager

class ExposedTransactionManager(private val database: Database) : TransactionManager {
    override suspend fun <T> transaction(transaction: suspend () -> T): Result<T> {
        if (currentCoroutineContext()[OutermostTransaction] != null) return attempt(transaction)
        return withContext(OutermostTransaction) { retryOnConflict(transaction) }
    }

    override suspend fun isInTransaction(): Boolean = JdbcTransactionManager.currentOrNull()?.db == database

    private suspend fun <T> retryOnConflict(transaction: suspend () -> T): Result<T> {
        var attempt = 1
        while (true) {
            val result = attempt(transaction)
            val shouldRetry = result.exceptionOrNull() is Error.TransactionConflict && attempt < MAX_ATTEMPTS_ON_CONFLICT
            if (!shouldRetry) return result
            delay(conflictBackoff(attempt))
            attempt++
        }
    }

    private suspend fun <T> attempt(transaction: suspend () -> T): Result<T> {
        return runSuspendCatching {
            suspendTransaction(db = database) {
                maxAttempts = 1
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

    private fun conflictBackoff(attempt: Int): Duration =
        (Random.nextLong(MIN_CONFLICT_BACKOFF_MILLIS, MAX_CONFLICT_BACKOFF_MILLIS) * attempt).milliseconds

    private object OutermostTransaction : CoroutineContext.Element, CoroutineContext.Key<OutermostTransaction> {
        override val key: CoroutineContext.Key<*> get() = this
    }

    companion object {
        const val MAX_ATTEMPTS_ON_CONFLICT = 3
        private const val MIN_CONFLICT_BACKOFF_MILLIS = 10L
        private const val MAX_CONFLICT_BACKOFF_MILLIS = 50L
    }
}
