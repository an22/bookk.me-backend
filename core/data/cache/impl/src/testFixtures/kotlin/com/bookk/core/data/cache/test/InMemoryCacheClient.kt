package com.bookk.core.data.cache.test

import com.bookk.core.data.cache.CacheClient
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.Expiry
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.serializer
import kotlin.reflect.KType
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

class InMemoryCacheClient(
    private val protobuf: ProtoBuf = ProtoBuf { encodeDefaults = true }
): CacheClient<String> {

    private class InMemCacheEntry(val value: ByteArray, val ttl: Duration)

    private val cache = Caffeine<String, InMemCacheEntry>.newBuilder()
        .expireAfter(Expiry.creating<String, InMemCacheEntry> { _, value -> value.ttl.toJavaDuration() })
        .maximumSize(10_000)
        .build<String, InMemCacheEntry>()


    override suspend fun <V : Any> set(
        key: String,
        value: V,
        kType: KType,
        expiration: Duration?
    ) {
        val serializer = protobuf.serializersModule.serializer(kType)
        val data = protobuf.encodeToByteArray(serializer, value)
        cache.put(key, InMemCacheEntry(data, expiration ?: 5.minutes))
    }

    override suspend fun <V : Any> get(key: String, kType: KType): V? {
        val serializer = protobuf.serializersModule.serializer(kType)
        return cache.getIfPresent(key)?.let {
            protobuf.decodeFromByteArray(serializer, it.value) as V
        }
    }

    override suspend fun withTransaction(action: suspend CacheClient<String>.() -> Unit) {
        this.action()
    }

    override suspend fun delete(key: String) {
        cache.invalidate(key)
    }

    override fun close() {
    }
}