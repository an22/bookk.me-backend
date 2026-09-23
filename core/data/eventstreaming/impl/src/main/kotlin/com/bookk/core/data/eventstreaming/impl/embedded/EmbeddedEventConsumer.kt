package com.bookk.core.data.eventstreaming.impl.embedded

import com.bookk.core.data.eventstreaming.DltEvent
import com.bookk.core.data.eventstreaming.DltRetry
import com.bookk.core.data.eventstreaming.EventStreaming.Consumer
import com.bookk.core.data.eventstreaming.EventStreaming.Event
import com.bookk.core.data.eventstreaming.ExhaustedEventDataSource
import io.ktor.util.collections.ConcurrentMap
import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.serializer
import kotlin.reflect.KType
import kotlin.time.Duration.Companion.milliseconds

class EmbeddedEventConsumer(
    private val topicQueueHolder: TopicQueueHolder<String>,
    private val protoBuf: ProtoBuf,
    private val exhaustedEventDataSource: ExhaustedEventDataSource,
    private val maxAttempts: Int = DltRetry.DEFAULT_MAX_ATTEMPTS,
) : Consumer<String> {

    private val logger = KtorSimpleLogger("EmbeddedEventConsumer")
    private val receivers = ConcurrentMap<String, suspend (Event<String>, Int) -> Unit>()
    private val types = ConcurrentMap<String, KType>()

    fun typeFor(originalTopic: String): KType? = types[originalTopic]

    @Suppress("UNCHECKED_CAST")
    override fun <T : Event<String>> registerReceiver(
        topic: String,
        type: KType,
        onEvent: suspend (T) -> Unit
    ): Consumer<String> {
        types[topic] = type
        receivers[topic] = { event, attempt ->
            runCatching { onEvent(event as T) }
                .onFailure {
                    handleFailure(topic, event, attempt)
                    logger.error("Error while processing event for topic: ${event.topic}. Event: $event")
                }
        }
        return this
    }

    override fun start(scope: CoroutineScope): Job {
        val originalTopics = receivers.keys.toList()
        originalTopics.forEach { topic ->
            receivers[DltRetry.dltTopic(topic)] = { event, _ -> retryFromDlt(event as EmbeddedDltEvent) }
        }
        return merge(
            *receivers.keys
                .map {
                    topicQueueHolder.subscribe(it)
                        .receiveAsFlow()
                        .onEach { event ->
                            try {
                                receivers[event.topic]?.invoke(event, 0)
                            } catch (e: Throwable) {
                                logger.error("Failed to handle: topic:{}, event:{}", event.topic, event)
                            }
                        }
                }
                .toTypedArray()
        )
            .onEach { logger.debug("Event received: topic:{}, event:{}", it.topic, it) }
            .launchIn(scope)
    }

    private suspend fun handleFailure(topic: String, event: Event<String>, attempt: Int) {
        val nextAttempt = attempt + 1
        val dltEvent = EmbeddedDltEvent(originalEvent = event, attempt = nextAttempt, topic = DltRetry.dltTopic(topic))
        if (DltRetry.isExhausted(nextAttempt, maxAttempts)) {
            persistOrRequeue(dltEvent)
        } else {
            topicQueueHolder.publish(dltEvent)
        }
    }

    private suspend fun retryFromDlt(dltEvent: EmbeddedDltEvent) {
        delay(DltRetry.backoffMillis(dltEvent.attempt).milliseconds)
        val originalTopic = dltEvent.originalEvent.topic
        if (DltRetry.isExhausted(dltEvent.attempt, maxAttempts)) {
            persistOrRequeue(dltEvent)
        } else {
            receivers[originalTopic]?.invoke(dltEvent.originalEvent, dltEvent.attempt)
        }
    }

    private suspend fun persistOrRequeue(dltEvent: EmbeddedDltEvent) {
        val originalTopic = dltEvent.originalEvent.topic
        val type = types[originalTopic] ?: return
        val encoded = protoBuf.encodeToByteArray(protoBuf.serializersModule.serializer(type), dltEvent.originalEvent)
        val record = DltEvent(
            payload = encoded,
            originalTopic = originalTopic,
            attempt = dltEvent.attempt,
            topic = originalTopic,
            idempotencyKey = dltEvent.idempotencyKey
        )
        runCatching { exhaustedEventDataSource.save(record) }
            .onFailure {
                logger.error("Failed to persist exhausted event for topic: {}. Retrying. Error: {}", originalTopic, it.message)
                topicQueueHolder.publish(dltEvent.copy(attempt = dltEvent.attempt + 1))
            }
    }
}
