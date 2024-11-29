package com.book.core.data.eventstreaming.di

import com.book.core.data.eventstreaming.EventStreaming
import com.book.core.data.eventstreaming.impl.KafkaEventConsumer
import com.book.core.data.eventstreaming.impl.KafkaEventProducer
import kotlinx.serialization.protobuf.ProtoBuf
import org.koin.core.qualifier.Qualifier
import org.koin.dsl.module

fun eventStreamingModule(qualifier: Qualifier) = module {
    factory<EventStreaming.Consumer<String, ByteArray>>(qualifier) {
        val servers = System.getenv("BOOKK_ME_KAFKA_HOSTS").split(',')
        KafkaEventConsumer(servers, ProtoBuf { encodeDefaults = true })
    }
    factory<EventStreaming.Producer<String, ByteArray>>(qualifier) {
        val servers = System.getenv("BOOKK_ME_KAFKA_HOSTS").split(',')
        KafkaEventProducer(servers, ProtoBuf { encodeDefaults = true })
    }
}