package com.bookk.core.data.eventstreaming.impl.embedded

import com.bookk.core.data.eventstreaming.DltEvent
import com.bookk.core.data.eventstreaming.EventStreaming
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.serializer
import kotlin.reflect.KType

class EmbeddedEventProducer(
    private val topicQueueHolder: TopicQueueHolder<String>,
    private val protoBuf: ProtoBuf,
) : EventStreaming.Producer<String> {

    override suspend fun <T : EventStreaming.Event<String>> send(data: T, kType: KType) {
        topicQueueHolder.publish(data)
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun replay(dltEvent: DltEvent, kType: KType) {
        val event = protoBuf.decodeFromByteArray(protoBuf.serializersModule.serializer(kType), dltEvent.payload) as EventStreaming.Event<String>
        topicQueueHolder.publish(event)
    }
}
