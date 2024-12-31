package com.book.core.data.cache

import java.io.Closeable
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlin.time.Duration

interface CacheClient<K> : Closeable {
    suspend fun <V : Any> set(key: K, value: V, kType: KType)
    suspend fun <V : Any> get(key: K, kType: KType): V?
    suspend fun <V : Any> withTransaction(action: CacheTransaction<K>.() -> Unit, kType: KType)
    suspend fun delete(key: K)

    interface CacheTransaction<K> {
        fun <V> set(key: K, value: V, kType: KType)
        fun <V> get(key: K, kType: KType): V?
        fun setExpiration(key: K, expiration: Duration)
        fun delete(key: K)
    }
}

suspend inline fun <K, reified V : Any> CacheClient<K>.set(key: K, value: V) {
    set(key, value, typeOf<V>())
}

suspend inline fun <K, reified V : Any> CacheClient<K>.get(key: K): V? {
    return get(key, typeOf<V>())
}

inline fun <K, reified V : Any> CacheClient.CacheTransaction<K>.set(key: K, value: V) {
    set(key, value, typeOf<V>())
}

inline fun <K, reified V : Any> CacheClient.CacheTransaction<K>.get(key: K): V? {
    return get(key, typeOf<V>())
}

suspend inline fun <K, reified V : Any> CacheClient<K>.withTransaction(noinline action: CacheClient.CacheTransaction<K>.() -> Unit) {
    withTransaction<V>(action, typeOf<V>())
}