package com.book.core.data.eventstreaming

import com.book.core.data.eventstreaming.EventStreaming.Consumer
import com.book.core.data.eventstreaming.EventStreaming.Producer
import kotlinx.coroutines.Job
import kotlin.reflect.KType
import kotlin.reflect.typeOf

interface EventStreaming {
    interface Consumer<K, V> {
        fun <T> registerReceiver(
            topic: K,
            type: KType,
            onEvent: (T) -> Unit
        ): Consumer<K, V>

        fun start(): Job
    }

    interface Producer<K, V> {
        suspend fun <T : Any> send(topic: K, data: T, kType: KType)
    }
}

suspend inline fun <reified T : Any, K, V> Producer<K, V>.send(topic: K, data: T) {
    send(topic, data, typeOf<T>())
}

inline fun <reified T : Any, K, V> Consumer<K, V>.registerReceiver(
    topic: K,
    noinline onEvent: (T) -> Unit
): Consumer<K, V> {
    return registerReceiver(topic, typeOf<T>(), onEvent)
}