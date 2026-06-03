package com.bookk.core.data.eventstreaming.impl.kafka

import com.bookk.core.data.eventstreaming.EventIdempotencyStorage
import com.bookk.core.data.eventstreaming.EventStreaming
import com.bookk.core.data.eventstreaming.EventStreaming.Event
import io.ktor.util.collections.ConcurrentMap
import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.serializer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.StringDeserializer
import java.time.Duration
import kotlin.reflect.KType

class KafkaEventConsumer(
    servers: List<String>,
    consumerGroup: String,
    private val eventIdempotencyStorage: EventIdempotencyStorage,
    private val protoBuf: ProtoBuf
) : EventStreaming.Consumer<String> {

    private val logger = KtorSimpleLogger("KafkaEventConsumer")
    private val receivers = ConcurrentMap<String, suspend (ByteArray) -> Unit>()
    private val consumer = KafkaConsumer(
        mutableMapOf<String, Any>(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to servers.joinToString { it },
            ConsumerConfig.GROUP_ID_CONFIG to consumerGroup,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest"
        ),
        StringDeserializer(),
        ByteArrayDeserializer()
    )

    @Suppress("UNCHECKED_CAST")
    override fun <T : Event<String>> registerReceiver(
        topic: String,
        type: KType,
        onEvent: suspend (T) -> Unit
    ): EventStreaming.Consumer<String> {
        receivers[topic] = {
            val serializer = protoBuf.serializersModule.serializer(type)
            val event = protoBuf.decodeFromByteArray(serializer, it) as T
            if (!eventIdempotencyStorage.isEventProcessed(event.topic, event.idempotencyKey)) {
                runCatching {
                    logger.debug("Received event, Topic: {}. Event: {}", event.topic, event)
                    onEvent(event)
                }.onSuccess {
                    eventIdempotencyStorage.markEventAsProcessed(event.topic, event.idempotencyKey)
                    logger.debug("Event successfully processed for topic: {}. Event: {}", event.topic, event)
                }.onFailure {
                    logger.error("Error while processing event for topic: ${event.topic}. Event: $event")
                }
            }
        }
        return this
    }

    override fun start(scope: CoroutineScope): Job {
        consumer.subscribe(receivers.keys)
        return flow {
            while (currentCoroutineContext().isActive) {
                emit(consumer.poll(Duration.ofSeconds(5)))
            }
        }
            .buffer(capacity = 100)
            .flatMapLatest { it.asFlow() }
            .onEach { record ->
                receivers[record.topic()]?.invoke(record.value())
            }
            .launchIn(scope)
    }
}