package com.book.user.data.datasource

import com.book.core.data.DataSource
import com.book.core.data.cache.CacheClient
import com.book.core.data.cache.get
import com.book.core.data.cache.set
import com.book.user.data.map.toDomain
import com.book.user.data.orm.entity.UserEntity
import com.book.user.domain.api.entity.User
import com.book.user.domain.datasource.UserDataSource
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.time.Duration.Companion.minutes

internal class UserDataSourceImpl(
    private val cacheClient: CacheClient<String>
) : DataSource(), UserDataSource {

    override suspend fun insertNewUser(user: User) = transaction {
        UserEntity.new {
            name = user.name
            lastName = user.lastName
            email = user.email
            phone = user.phone
            updatedAt = Clock.System.now()
        }.toDomain()
    }

    override suspend fun getUserById(id: Long): User? {
        val key = "${USER_CACHE_KEY}$id"
        val cached: User? = cacheClient.get(key)
        if (cached != null) return cached
        val user = transaction { UserEntity[id].toDomain() }
        cacheClient.withTransaction {
            set(key, user)
            setExpiration(key, 10.minutes)
        }
        return user
    }

    override suspend fun deleteUser(id: Long) {
        transaction { UserEntity[id].delete() }
        cacheClient.delete("${USER_CACHE_KEY}$id")
    }

    companion object {
        const val USER_CACHE_KEY = "user:"
    }
}