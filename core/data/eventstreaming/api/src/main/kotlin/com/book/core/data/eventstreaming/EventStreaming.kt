package com.book.core.data.eventstreaming

import com.book.core.data.eventstreaming.EventStreaming.Consumer
import com.book.core.data.eventstreaming.EventStreaming.Producer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.reflect.KType
import kotlin.reflect.typeOf

interface EventStreaming {
    interface Consumer<K> {
        fun <T> registerReceiver(
            topic: K,
            type: KType,
            onEvent: suspend (T) -> Unit
        ): Consumer<K>

        fun start(scope: CoroutineScope): Job
    }

    interface Producer<K> {
        suspend fun <T : Any> send(topic: K, data: T, kType: KType)
    }
}

suspend inline fun <reified T : Any, K> Producer<K>.send(topic: K, data: T) {
    send(topic, data, typeOf<T>())
}

inline fun <reified T : Any, K> Consumer<K>.registerReceiver(
    topic: K,
    noinline onEvent: suspend (T) -> Unit
): Consumer<K> {
    return registerReceiver(topic, typeOf<T>(), onEvent)
}

typealias StandardEventProducer = Producer<String>

typealias StandardEventConsumer = Consumer<String>