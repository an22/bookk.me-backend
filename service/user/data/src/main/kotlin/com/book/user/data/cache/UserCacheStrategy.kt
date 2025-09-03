package com.book.user.data.cache

import com.book.core.data.cache.CacheClient
import com.book.core.data.cache.get
import com.book.core.data.cache.set
import com.book.user.domain.api.entity.User
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.Uuid

object UserCacheStrategy {
    private val cacheExpiration = 10.minutes

    suspend fun CacheClient<String>.save(user: User) {
        withTransaction {
            val key = cacheKeyFrom(user)
            set(key, user)
            setExpiration(key, cacheExpiration)
        }
    }

    suspend fun CacheClient<String>.getUser(id: Uuid): User? {
        return get(cacheKeyFrom(id))
    }

    suspend fun CacheClient<String>.deleteUser(id: Uuid) {
        delete(cacheKeyFrom(id))
    }

    private fun cacheKeyFrom(user: User): String {
        return cacheKeyFrom(user.id)
    }

    private fun cacheKeyFrom(id: Uuid): String {
        return "user_$id"
    }
}