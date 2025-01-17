package com.book.core.data

import com.book.core.data.map.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

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

    fun <T> Flow<T>.handleErrors(): Flow<T> {
        return catch { e ->
            throw e.toDomain()
        }
    }
}