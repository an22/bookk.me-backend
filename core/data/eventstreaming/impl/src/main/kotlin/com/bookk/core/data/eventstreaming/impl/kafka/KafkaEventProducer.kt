package com.bookk.core.data.eventstreaming.impl.kafka

import com.bookk.core.data.eventstreaming.EventStreaming
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.serializer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.serialization.StringSerializer
import kotlin.coroutines.resume
import kotlin.reflect.KType

class KafkaEventProducer(
    servers: List<String>,
    client: String,
    private val protoBuf: ProtoBuf
) : EventStreaming.Producer<String> {

    private val producer = KafkaProducer(
        mapOf(
            ProducerConfig.CLIENT_ID_CONFIG to client,
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to servers.joinToString { it },
            ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG to "true",
            ProducerConfig.RETRIES_CONFIG to 10,
            ProducerConfig.ACKS_CONFIG to "all"
        ),
        StringSerializer(),
        ByteArraySerializer()
    )


    override suspend fun <T : EventStreaming.Event<String>> send(data: T, kType: KType) {
        suspendCancellableCoroutine { continuation ->
            val encodedData = protoBuf.encodeToByteArray(protoBuf.serializersModule.serializer(kType), data)
            producer.send(ProducerRecord(data.topic, data.partitionKey, encodedData)) { metadata, exception ->
                when {
                    metadata != null -> continuation.resume(Unit)
                    else -> continuation.cancel(exception)
                }
            }
        }
    }
}