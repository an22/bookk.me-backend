package com.book.core.data.eventstreaming.impl.embedded

import com.book.core.data.eventstreaming.EventStreaming.Consumer
import com.book.core.data.eventstreaming.EventStreaming.Event
import io.ktor.util.collections.ConcurrentMap
import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlin.reflect.KType

class EmbeddedEventConsumer(
    private val topicQueueHolder: TopicQueueHolder<String>,
) : Consumer<String> {

    private val logger = KtorSimpleLogger("EmbeddedEventConsumer")
    private val receivers = ConcurrentMap<String, suspend (Event<String>) -> Unit>()

    @Suppress("UNCHECKED_CAST")
    override fun <T : Event<String>> registerReceiver(
        topic: String,
        type: KType,
        onEvent: suspend (T) -> Unit
    ): Consumer<String> {
        receivers[topic] = { event ->
            runCatching { onEvent(event as T) }
                .onFailure {
                    logger.error("Error while processing event for topic: ${event.topic}. Event: $event")
                }
        }
        return this
    }

    override fun start(scope: CoroutineScope): Job {
        return merge(
            *receivers.keys
                .map {
                    topicQueueHolder.get(it)
                        .receiveAsFlow()
                        .onEach { event ->
                            try {
                                receivers[event.topic]?.invoke(event)
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
}