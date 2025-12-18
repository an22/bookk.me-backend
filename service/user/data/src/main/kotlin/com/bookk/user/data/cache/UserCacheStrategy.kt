package com.bookk.user.data.cache

import com.bookk.core.data.cache.CacheClient
import com.bookk.core.data.cache.get
import com.bookk.core.data.cache.set
import com.bookk.user.domain.api.entity.User
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