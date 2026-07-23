package com.bookk.core.data.cache

import java.io.Closeable
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlin.time.Duration

interface CacheClient<K> : Closeable {
    suspend fun <V : Any> set(key: K, value: V, kType: KType, expiration: Duration?)
    suspend fun <V : Any> get(key: K, kType: KType): V?
    suspend fun withTransaction(action: suspend CacheClient<K>.() -> Unit)
    suspend fun delete(key: K)
}

suspend inline fun <K, reified V : Any> CacheClient<K>.set(key: K, value: V, ttl: Duration? = null) {
    set(key, value, typeOf<V>(), ttl)
}

suspend inline fun <K, reified V : Any> CacheClient<K>.get(key: K): V? {
    return get(key, typeOf<V>())
}