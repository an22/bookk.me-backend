package com.bookk.core.data.eventstreaming.impl.kafka

import com.bookk.core.data.eventstreaming.DltEvent
import com.bookk.core.data.eventstreaming.EventIdempotencyStorage
import com.bookk.core.data.eventstreaming.EventStreaming
import com.bookk.core.data.eventstreaming.EventStreaming.Event
import com.bookk.core.data.eventstreaming.send
import io.ktor.util.collections.ConcurrentMap
import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.isActive
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.serializer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.StringDeserializer
import java.time.Duration
import kotlin.reflect.KType

class KafkaEventConsumer(
    servers: List<String>,
    consumerGroup: String,
    private val eventIdempotencyStorage: EventIdempotencyStorage,
    private val protoBuf: ProtoBuf,
    private val dltProducer: EventStreaming.Producer<String>,
) : EventStreaming.Consumer<String> {

    private val logger = KtorSimpleLogger("KafkaEventConsumer")
    private val receivers = ConcurrentMap<String, suspend (ByteArray) -> Unit>()
    private val consumer = KafkaConsumer(
        mutableMapOf<String, Any>(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to servers.joinToString { it },
            ConsumerConfig.GROUP_ID_CONFIG to consumerGroup,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to "false"
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
            runCatching {
                val serializer = protoBuf.serializersModule.serializer(type)
                protoBuf.decodeFromByteArray(serializer, it) as T
            }.onFailure {
                logger.debug("Received unprocessable event, Topic: {}. Error: {}", topic, it.message)
            }.onSuccess { event ->
                if (!eventIdempotencyStorage.isEventProcessed(event.topic, event.idempotencyKey)) {
                    runCatching {
                        logger.debug("Received event, Topic: {}. Event: {}", event.topic, event)
                        onEvent(event)
                    }.onSuccess {
                        eventIdempotencyStorage.markEventAsProcessed(event.topic, event.idempotencyKey)
                        logger.debug("Event successfully processed for topic: {}. Event: {}", event.topic, event)
                    }.onFailure { error ->
                        dltProducer.send(DltEvent(event.toString(), "${event.topic}_dlt"))
                        logger.error("Error while processing event for topic: ${event.topic}. Event: $event, $error")
                    }
                }
            }
        }
        return this
    }

    override fun start(scope: CoroutineScope): Job {
        consumer.subscribe(receivers.keys)
        return flow {
            while (currentCoroutineContext().isActive) {
                val records = consumer.poll(Duration.ofSeconds(1))
                if (!records.isEmpty) {
                    emit(records)
                }
            }
        }
            .onEach { records ->
                coroutineScope {
                    records.map { processRecord(it) }.awaitAll()
                    consumer.commitSync()
                }
            }
            .retry()
            .launchIn(scope)
    }

    private fun CoroutineScope.processRecord(record: ConsumerRecord<String, ByteArray>) = async {
        receivers[record.topic()]?.invoke(record.value())
    }
}