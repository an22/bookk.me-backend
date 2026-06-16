package com.bookk.core.data.repository

import com.bookk.core.data.cache.CacheClient
import com.bookk.core.data.cache.get
import com.bookk.core.data.cache.set
import com.bookk.core.data.eventstreaming.EventIdempotencyStorage
import kotlinx.serialization.Serializable
import library.idempotency.IdempotencyKey
import library.idempotency.IdempotencyResponse
import library.idempotency.IdempotentResponseRepository
import java.time.OffsetDateTime
import kotlin.time.Duration.Companion.minutes

@Serializable
private class SerializableIdempotencyResponse(
    val isInProgress: Boolean,
    val response: ByteArray
)

class CacheIdempotentResponseRepository(
    private val cacheClient: CacheClient<String>
) : IdempotentResponseRepository, EventIdempotencyStorage {

    override suspend fun storeResponse(
        resource: String,
        idempotencyKey: IdempotencyKey,
        response: ByteArray
    ) {
        val key = "$resource:$idempotencyKey"
        val value = SerializableIdempotencyResponse(isInProgress = false, response = response)
        cacheClient.withTransaction {
            set(key, value)
            setExpiration(key, 10.minutes)
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
                setExpiration(key, 10.minutes)
            }
        }
        return response?.let {
            IdempotencyResponse(
                isInProgress = it.isInProgress,
                response = it.response
            )
        }
    }

    override suspend fun release(resource: String, idempotencyKey: IdempotencyKey) {
        val key = "$resource:$idempotencyKey"
        cacheClient.withTransaction {
            delete(key)
        }
    }

    override suspend fun deleteExpiredResponses(lastValidDate: OffsetDateTime) {
        //Responses are deleted automatically
    }

    override suspend fun markEventAsProcessed(resource: String, idempotencyKey: String) {
        storeResponse(resource, IdempotencyKey(idempotencyKey), ByteArray(0))
    }

    override suspend fun isEventProcessed(resource: String, idempotencyKey: String): Boolean {
        return getResponseOrLock(resource, IdempotencyKey(idempotencyKey)) != null
    }
}