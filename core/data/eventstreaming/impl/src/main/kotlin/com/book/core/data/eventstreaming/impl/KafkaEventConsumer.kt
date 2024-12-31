package com.book.core.data.eventstreaming.impl

import com.book.core.data.eventstreaming.EventStreaming
import com.bookk.core.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
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
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.StringDeserializer
import java.time.Duration
import java.util.*
import kotlin.reflect.KType

class KafkaEventConsumer(
    servers: List<String>,
    private val protoBuf: ProtoBuf
) : EventStreaming.Consumer<String, ByteArray> {

    private val consumerScope = CoroutineScope(DispatcherProvider.io + SupervisorJob())
    private val receivers = mutableMapOf<String, suspend (ByteArray) -> Unit>()
    private val consumer = KafkaConsumer(
        mutableMapOf<String, Any>(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to servers.joinToString { it },
            CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to "SSL",
            SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG to "",
            SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG to "",
            ConsumerConfig.GROUP_ID_CONFIG to UUID.randomUUID().toString(),
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest"
        ),
        StringDeserializer(),
        ByteArrayDeserializer()
    )

    @Suppress("UNCHECKED_CAST")
    override fun <T> registerReceiver(
        topic: String,
        type: KType,
        onEvent: (T) -> Unit
    ): EventStreaming.Consumer<String, ByteArray> {
        receivers[topic] = {
            val serializer = protoBuf.serializersModule.serializer(type)
            onEvent(protoBuf.decodeFromByteArray(serializer, it) as T)
        }
        return this
    }

    override fun start(): Job = flow {
        while (currentCoroutineContext().isActive) {
            emit(consumer.poll(Duration.ofSeconds(5)))
        }
    }.buffer(capacity = 100)
        .flatMapLatest { it.asFlow() }
        .onEach { record ->
            receivers[record.topic()]?.invoke(record.value())
        }
        .launchIn(consumerScope)
}