package com.bookk.core.data.eventstreaming

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class DltEvent(
    val payload: String,
    override val topic: String,
    override val idempotencyKey: String = Uuid.random().toString()
): EventStreaming.Event<String>