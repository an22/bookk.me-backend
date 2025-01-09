package com.book.core.data

import com.book.core.data.map.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

abstract class DataSource {

    suspend fun <T> dbTransaction(
        finally: () -> Unit = {},
        action: suspend () -> T
    ): T {
        return try {
            newSuspendedTransaction {
                action()
            }
        } catch (e: Exception) {
            throw e.toDomain()
        } finally {
            finally()
        }
    }

    suspend fun <T> execute(
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

    fun <T> Flow<T>.handleErrors(): Flow<T> {
        return catch { e ->
            throw e.toDomain()
        }
    }
}