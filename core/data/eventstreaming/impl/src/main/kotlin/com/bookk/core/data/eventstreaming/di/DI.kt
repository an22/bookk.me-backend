package com.bookk.core.data.eventstreaming.di

import com.bookk.core.AppLevelConstants
import com.bookk.core.data.eventstreaming.StandardEventConsumer
import com.bookk.core.data.eventstreaming.StandardEventProducer
import com.bookk.core.data.eventstreaming.impl.embedded.EmbeddedEventConsumer
import com.bookk.core.data.eventstreaming.impl.embedded.EmbeddedEventProducer
import com.bookk.core.data.eventstreaming.impl.embedded.TopicQueueHolder
import com.bookk.core.data.eventstreaming.impl.kafka.KafkaEventConsumer
import com.bookk.core.data.eventstreaming.impl.kafka.KafkaEventProducer
import kotlinx.serialization.protobuf.ProtoBuf
import org.koin.core.qualifier.Qualifier
import org.koin.dsl.module

fun eventStreamingModule(qualifier: Qualifier, serviceName: String) = module {
    scope(qualifier) {
        scoped<StandardEventConsumer> {
            val servers = AppLevelConstants.eventStreamingHost.split(',')
            val group = "${serviceName}_group"
            KafkaEventConsumer(servers, group, get(), ProtoBuf { encodeDefaults = true }, get())
        }
        scoped<StandardEventProducer> {
            val servers = AppLevelConstants.eventStreamingHost.split(',')
            val clientName = "${serviceName}_producer"
            KafkaEventProducer(servers, clientName, ProtoBuf { encodeDefaults = true })
        }
    }
}

fun topicQueueHolderModule() = module {
    single { TopicQueueHolder<String>() }
}

fun embeddedEventStreamingModule(qualifier: Qualifier) = module {
    scope(qualifier) {
        scoped<StandardEventConsumer> { EmbeddedEventConsumer(get()) }
        scoped<StandardEventProducer> { EmbeddedEventProducer(get()) }
    }
}