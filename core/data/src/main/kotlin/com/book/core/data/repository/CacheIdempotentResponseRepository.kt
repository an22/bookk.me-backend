package com.book.core.data.repository

import com.book.core.data.cache.CacheClient
import com.book.core.data.cache.get
import com.book.core.data.cache.set
import com.wolt.utils.ktor.idempotency.IdempotencyKey
import com.wolt.utils.ktor.idempotency.IdempotencyResponse
import com.wolt.utils.ktor.idempotency.IdempotentResponseRepository
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime
import kotlin.time.Duration.Companion.days

@Serializable
private class SerializableIdempotencyResponse(
    val isInProgress: Boolean,
    val response: ByteArray
)

class CacheIdempotentResponseRepository(
    private val cacheClient: CacheClient<String>
) : IdempotentResponseRepository {


    override suspend fun storeResponse(
        resource: String,
        idempotencyKey: IdempotencyKey,
        response: ByteArray
    ) {
        val key = "$resource:$idempotencyKey"
        val value = SerializableIdempotencyResponse(isInProgress = false, response = response)
        cacheClient.withTransaction {
            set(key, value)
            setExpiration(key, 1.days)
        }
    }

    override suspend fun getResponseOrLock(
        resource: String,
        idempotencyKey: IdempotencyKey
    ): IdempotencyResponse? {
        val key = "$resource:$idempotencyKey"
        val response: SerializableIdempotencyResponse? = cacheClient.get(key)
        if (response == null) {
            cacheClient.withTransaction {
                val lock = SerializableIdempotencyResponse(isInProgress = true, response = ByteArray(0))
                set(key, lock)
                setExpiration(key, 1.days)
            }
        }
        return response?.let {
            IdempotencyResponse(
                isInProgress = it.isInProgress,
                response = it.response
            )
        }
    }

    override suspend fun deleteExpiredResponses(lastValidDate: OffsetDateTime) {
    }
}