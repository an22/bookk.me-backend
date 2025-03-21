package com.book.core.data.eventstreaming

import com.book.core.data.eventstreaming.EventStreaming.Consumer
import com.book.core.data.eventstreaming.EventStreaming.Event
import com.book.core.data.eventstreaming.EventStreaming.Producer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.reflect.KType
import kotlin.reflect.typeOf

interface EventStreaming {
    interface Consumer<K> {
        fun <T : Event<K>> registerReceiver(
            topic: K,
            type: KType,
            onEvent: suspend (T) -> Unit
        ): Consumer<K>

        fun start(scope: CoroutineScope): Job
    }

    interface Producer<K> {
        suspend fun <T : Event<K>> send(data: T, kType: KType)
    }

    interface Event<K> {
        val topic: K
        val idempotencyKey: String
    }
}

suspend inline fun <reified T : Event<K>, K> Producer<K>.send(data: T) {
    send(data, typeOf<T>())
}

inline fun <reified T : Any, K> Consumer<K>.registerReceiver(
    topic: K,
    noinline onEvent: suspend (T) -> Unit
): Consumer<K> {
    return registerReceiver(topic, typeOf<T>(), onEvent)
}

typealias StandardEventProducer = Producer<String>

typealias StandardEventConsumer = Consumer<String>