package com.book.user.data.repository

import com.book.core.data.DataSource
import com.book.core.data.cache.CacheClient
import com.book.core.data.cache.get
import com.book.core.data.cache.set
import com.book.core.data.cache.withTransaction
import com.book.user.data.map.toDomain
import com.book.user.data.orm.entity.UserEntity
import com.book.user.domain.api.datasource.UserDataSource
import com.book.user.domain.api.entity.User
import kotlin.time.Duration.Companion.minutes

internal class UserDataSourceImpl(
    private val cacheClient: CacheClient<String>
) : DataSource(), UserDataSource {

    override suspend fun insertNewUser(user: User) = dbTransaction {
        UserEntity.new {
            name = user.name
            lastName = user.lastName
            email = user.email
            phone = user.phone
        }.toDomain()
    }

    override suspend fun getUserById(id: Long): User? = dbTransaction {
        val key = "${USER_CACHE_KEY}$id"
        val cached: User? = cacheClient.get(key)
        if (cached != null) return@dbTransaction cached
        val user = UserEntity[id].toDomain()
        cacheClient.withTransaction<_, User> {
            set(key, user)
            setExpiration(key, 10.minutes)
        }
        return@dbTransaction user
    }

    override suspend fun deleteUser(id: Long) = dbTransaction {
        UserEntity[id].delete()
        cacheClient.delete("${USER_CACHE_KEY}$id")
    }

    companion object {
        const val USER_CACHE_KEY = "user:"
    }
}