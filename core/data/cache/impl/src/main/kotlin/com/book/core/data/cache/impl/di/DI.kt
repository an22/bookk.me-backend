package com.book.core.data.cache.impl.di

import com.book.core.data.cache.CacheClient
import com.book.core.data.cache.impl.RedisCacheClient
import kotlinx.serialization.protobuf.ProtoBuf
import org.koin.core.qualifier.Qualifier
import org.koin.dsl.module

fun cacheModule(qualifier: Qualifier) = module {
    single<CacheClient<String>>(qualifier) {
        RedisCacheClient(
            host = System.getenv("me.bookk.redis_host"),
            port = System.getenv("me.bookk.redis_port").toInt(),
            protobuf = ProtoBuf { encodeDefaults = true }
        )
    }
}