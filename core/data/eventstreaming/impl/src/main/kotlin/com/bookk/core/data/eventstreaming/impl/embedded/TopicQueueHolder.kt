package com.bookk.core.data.eventstreaming.impl.embedded

import com.bookk.core.data.eventstreaming.EventStreaming.Event
import io.ktor.util.collections.ConcurrentMap
import kotlinx.coroutines.channels.Channel

class TopicQueueHolder<Key : Any> {

    private val topicQueueMap = ConcurrentMap<Key, Channel<Event<Key>>>()

    fun get(topic: Key): Channel<Event<Key>> {
        return topicQueueMap.getOrPut(topic) {
            Channel(Channel.UNLIMITED)
        }
    }
}