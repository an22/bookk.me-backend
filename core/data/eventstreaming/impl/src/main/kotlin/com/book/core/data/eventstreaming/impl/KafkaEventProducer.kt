package com.book.core.data.eventstreaming.impl

import com.book.core.data.eventstreaming.EventStreaming
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
    private val protoBuf: ProtoBuf
) : EventStreaming.Producer<String, ByteArray> {

    private val producer = KafkaProducer(
        mapOf(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to servers.joinToString { it }),
        StringSerializer(),
        ByteArraySerializer()
    )

    override suspend fun <T : Any> send(topic: String, data: T, kType: KType) {
        suspendCancellableCoroutine<Unit> { continuation ->
            val encodedData = protoBuf.encodeToByteArray(protoBuf.serializersModule.serializer(kType), data)
            producer.send(ProducerRecord(topic, encodedData)) { metadata, exception ->
                when {
                    metadata != null -> continuation.resume(Unit)
                    else -> continuation.cancel(exception)
                }
            }
        }
    }
}