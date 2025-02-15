package com.book.user.data.datasource

import com.book.core.data.DataSource
import com.book.core.data.cache.CacheClient
import com.book.user.data.cache.UserCacheStrategy.deleteUser
import com.book.user.data.cache.UserCacheStrategy.getUser
import com.book.user.data.cache.UserCacheStrategy.save
import com.book.user.data.map.toDomain
import com.book.user.data.orm.entity.UserEntity
import com.book.user.data.orm.table.UserTable
import com.book.user.domain.api.entity.User
import com.book.user.domain.datasource.UserDataSource
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

internal class UserDataSourceImpl(
    private val cacheClient: CacheClient<String>
) : DataSource(), UserDataSource {

    override suspend fun insertNewUser(user: User) = mapExceptions {
        val createdUser = transaction {
            UserEntity.new {
                name = user.name
                lastName = user.lastName
                email = user.email
                updatedAt = Clock.System.now()
            }.toDomain()
        }
        cacheClient.save(user)
        return@mapExceptions createdUser
    }

    override suspend fun updateUser(user: User): Boolean {
        return mapExceptions {
            val updatedRowCount = transaction {
                UserTable.update(where = { UserTable.id eq user.id }) {
                    it[name] = user.name
                    it[lastName] = user.lastName
                    it[email] = user.email
                }
            }
            val isUpdated = updatedRowCount > 0
            if (isUpdated) {
                cacheClient.save(user)
            }
            isUpdated
        }
    }

    override suspend fun getUserById(id: Long): User? = mapExceptions {
        val cached: User? = cacheClient.getUser(id)
        if (cached != null) return@mapExceptions cached
        val user = transaction { UserEntity.findById(id)?.toDomain() }
        if (user != null) {
            cacheClient.save(user)
        }
        return@mapExceptions user
    }

    override suspend fun deleteUser(id: Long) = mapExceptions {
        transaction { UserEntity.findById(id)?.delete() }
        cacheClient.deleteUser(id)
    }
}