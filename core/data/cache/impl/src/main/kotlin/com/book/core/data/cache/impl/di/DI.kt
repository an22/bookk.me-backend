package com.book.core.data.cache.impl.di

import com.book.core.data.cache.CacheClient
import com.book.core.data.cache.impl.RedisCacheClient
import kotlinx.serialization.protobuf.ProtoBuf
import org.koin.dsl.module

fun cacheModule() = module {
    single<CacheClient<String>> {
        RedisCacheClient(
            host = System.getenv("BOOKK_ME_REDIS_HOSTS"),
            port = System.getenv("BOOKK_ME_REDIS_PORT").toInt(),
            protobuf = ProtoBuf { encodeDefaults = true },
            password = System.getenv("BOOKK_ME_REDIS_PASSWORD")
        )
    }
}