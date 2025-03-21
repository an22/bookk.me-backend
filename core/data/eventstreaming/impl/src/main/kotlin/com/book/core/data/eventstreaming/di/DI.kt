package com.book.core.data.eventstreaming.di

import com.book.core.data.eventstreaming.StandardEventConsumer
import com.book.core.data.eventstreaming.StandardEventProducer
import com.book.core.data.eventstreaming.impl.kafka.KafkaEventConsumer
import com.book.core.data.eventstreaming.impl.kafka.KafkaEventProducer
import kotlinx.serialization.protobuf.ProtoBuf
import org.koin.dsl.module

fun eventStreamingModule() = module {
    factory<StandardEventConsumer> {
        val servers = System.getenv("BOOKK_ME_KAFKA_HOSTS").split(',')
        val group = "${System.getenv("BOOKK_ME_SERVICE_NAME")}_group"
        KafkaEventConsumer(servers, group, get(), ProtoBuf { encodeDefaults = true })
    }
    factory<StandardEventProducer> {
        val servers = System.getenv("BOOKK_ME_KAFKA_HOSTS").split(',')
        val clientName = "${System.getenv("BOOKK_ME_SERVICE_NAME")}_producer"
        KafkaEventProducer(servers, clientName, ProtoBuf { encodeDefaults = true })
    }
}