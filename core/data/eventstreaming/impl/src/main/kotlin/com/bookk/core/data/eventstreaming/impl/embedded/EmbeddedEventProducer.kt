package com.bookk.core.data.eventstreaming.impl.embedded

import com.bookk.core.data.eventstreaming.EventStreaming
import kotlin.reflect.KType

class EmbeddedEventProducer(
    private val topicQueueHolder: TopicQueueHolder<String>
) : EventStreaming.Producer<String> {

    override suspend fun <T : EventStreaming.Event<String>> send(data: T, kType: KType) {
        topicQueueHolder.get(data.topic).send(data)
    }
}