package com.book.core.data.cache.impl.di

import com.book.core.data.cache.CacheClient
import com.book.core.data.cache.impl.RedisCacheClient
import com.bookk.core.AppLevelConstants
import kotlinx.serialization.protobuf.ProtoBuf
import org.koin.dsl.module

fun cacheModule() = module {
    single<CacheClient<String>> {
        RedisCacheClient(
            host = AppLevelConstants.cacheHost,
            port = AppLevelConstants.cachePort.toInt(),
            protobuf = ProtoBuf { encodeDefaults = true },
            password = AppLevelConstants.cachePass
        )
    }
}