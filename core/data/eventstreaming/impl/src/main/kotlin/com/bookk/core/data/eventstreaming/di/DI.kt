package com.bookk.core.data.eventstreaming.di

import com.bookk.core.AppLevelConstants
import com.bookk.core.data.eventstreaming.StandardEventConsumer
import com.bookk.core.data.eventstreaming.StandardEventProducer
import com.bookk.core.data.eventstreaming.impl.kafka.KafkaEventConsumer
import com.bookk.core.data.eventstreaming.impl.kafka.KafkaEventProducer
import kotlinx.serialization.protobuf.ProtoBuf
import org.koin.dsl.module

fun eventStreamingModule() = module {
    factory<StandardEventConsumer> {
        val servers = AppLevelConstants.eventStreamingHost.split(',')
        val group = "${AppLevelConstants.serviceName}_group"
        KafkaEventConsumer(servers, group, get(), ProtoBuf { encodeDefaults = true }, get())
    }
    factory<StandardEventProducer> {
        val servers = AppLevelConstants.eventStreamingHost.split(',')
        val clientName = "${AppLevelConstants.serviceName}_producer"
        KafkaEventProducer(servers, clientName, ProtoBuf { encodeDefaults = true })
    }
}