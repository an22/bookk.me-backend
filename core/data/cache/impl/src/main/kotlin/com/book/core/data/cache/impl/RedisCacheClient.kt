package com.book.core.data.cache.impl

import com.book.core.data.cache.CacheClient
import com.book.core.data.cache.impl.codec.ProtobufRedisCodec
import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.coroutines
import io.lettuce.core.api.sync.RedisCommands
import io.lettuce.core.api.sync.multi
import io.lettuce.core.support.ConnectionPoolSupport
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.serializer
import java.nio.ByteBuffer
import kotlin.reflect.KType
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

@OptIn(ExperimentalLettuceCoroutinesApi::class)
class RedisCacheClient(
    host: String,
    port: Int,
    password: CharSequence,
    private val protobuf: ProtoBuf
) : CacheClient<String> {

    private val client = RedisClient.create(
        RedisURI.builder()
            .withHost(host)
            .withPort(port)
            .withPassword(password)
            .withTimeout(5.seconds.toJavaDuration())
            .build()
    )
    private val connectionPool =
        ConnectionPoolSupport.createSoftReferenceObjectPool { client.connect(ProtobufRedisCodec()) }

    override suspend fun <V : Any> set(key: String, value: V, kType: KType) {
        with(connectionPool.borrowObject()) {
            val serializer = protobuf.serializersModule.serializer(kType)
            coroutines().apply {
                set(key, ByteBuffer.wrap(protobuf.encodeToByteArray(serializer, value)))
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun <V : Any> get(key: String, kType: KType): V? {
        return with(connectionPool.borrowObject()) {
            val deserializer = protobuf.serializersModule.serializer(kType)
            coroutines().get(key)?.let { buffer ->
                val array = ByteArray(buffer.remaining()).also { buffer.get(it) }
                protobuf.decodeFromByteArray(deserializer, array) as V
            }
        }
    }

    override suspend fun withTransaction(action: CacheClient.CacheTransaction<String>.() -> Unit) {
        with(connectionPool.borrowObject()) {
            sync().multi {
                action(asCacheTransactionTransaction())
            }
        }
    }

    override suspend fun delete(key: String) {
        with(connectionPool.borrowObject()) {
            coroutines().del(key)
        }
    }

    override fun close() {
        connectionPool.close()
        client.shutdown()
    }

    private fun <K> RedisCommands<K, ByteBuffer>.asCacheTransactionTransaction() =
        object : CacheClient.CacheTransaction<K> {
            override fun <V> set(key: K, value: V, kType: KType) {
                val serializer = protobuf.serializersModule.serializer(kType)
                this@asCacheTransactionTransaction.set(
                    key,
                    ByteBuffer.wrap(protobuf.encodeToByteArray(serializer, value))
                )
            }

            @Suppress("UNCHECKED_CAST")
            override fun <V> get(key: K, kType: KType): V? {
                val deserializer = protobuf.serializersModule.serializer(kType)
                return get(key)?.let { protobuf.decodeFromByteArray(deserializer, it.array()) as V }
            }

            override fun setExpiration(key: K, expiration: Duration) {
                this@asCacheTransactionTransaction.expire(key, expiration.toJavaDuration())
            }

            override fun delete(key: K) {
                this@asCacheTransactionTransaction.del(key)
            }

        }
}