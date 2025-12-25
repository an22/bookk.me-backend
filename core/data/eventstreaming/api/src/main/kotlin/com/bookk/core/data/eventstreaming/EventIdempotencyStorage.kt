package com.bookk.core.data.eventstreaming

interface EventIdempotencyStorage {
    suspend fun markEventAsProcessed(
        resource: String,
        idempotencyKey: String
    )

    suspend fun isEventProcessed(
        resource: String,
        idempotencyKey: String
    ): Boolean
}