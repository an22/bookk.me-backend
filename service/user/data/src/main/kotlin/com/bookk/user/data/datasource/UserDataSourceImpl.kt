package com.bookk.user.data.datasource

import com.bookk.core.data.DataSource
import com.bookk.core.data.cache.CacheClient
import com.bookk.user.data.cache.UserCacheStrategy.deleteUser
import com.bookk.user.data.cache.UserCacheStrategy.getUser
import com.bookk.user.data.cache.UserCacheStrategy.save
import com.bookk.user.data.map.toDomain
import com.bookk.user.data.orm.entity.UserEntity
import com.bookk.user.data.orm.table.UserTable
import com.bookk.user.domain.api.entity.User
import com.bookk.user.domain.api.entity.UserEditModel
import com.bookk.user.domain.datasource.UserDataSource
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insertAndGetId
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.update
import kotlin.time.Clock
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

internal class UserDataSourceImpl(
    private val cacheClient: CacheClient<String>
) : DataSource(), UserDataSource {

    override suspend fun insertNewUser(user: User) = mapExceptions {
        val createdUser = dbQuery {
            UserTable.insertAndGetId {
                it[name] = user.name
                it[lastName] = user.lastName
                it[email] = user.email
                it[updatedAt] = Clock.System.now()
            }.let {
                user.copy(id = it.value.toKotlinUuid())
            }
        }
        cacheClient.save(createdUser)
        return@mapExceptions createdUser
    }

    override suspend fun updateUser(id: Uuid, user: UserEditModel): Boolean {
        return mapExceptions {
            val updatedRowCount = dbQuery {
                UserTable.update(where = { UserTable.id eq id.toJavaUuid() }) {
                    user.firstName?.let { firstName ->
                        it[name] = firstName
                    }
                    user.lastName?.let { lstName ->
                        it[lastName] = lstName
                    }
                    user.email?.let { mail ->
                        it[email] = mail
                    }
                }
            }
            val isUpdated = updatedRowCount > 0
            if (isUpdated) {
                cacheClient.deleteUser(id)
            }
            isUpdated
        }
    }

    override suspend fun getUserById(id: Uuid): User? = mapExceptions {
        val cached: User? = cacheClient.getUser(id)
        if (cached != null) return@mapExceptions cached
        val user = dbQuery {
            UserTable.selectAll()
                .where { UserTable.id eq id.toJavaUuid() }
                .map { UserEntity.wrapRowR2dbc(it).toDomain() }
                .singleOrNull()
        }
        if (user != null) {
            cacheClient.save(user)
        }
        return@mapExceptions user
    }

    override suspend fun getUserByEmail(email: String): User? = mapExceptions {
        dbQuery {
            UserTable.selectAll()
                .where { UserTable.email eq email }
                .map { UserEntity.wrapRowR2dbc(it).toDomain() }
                .singleOrNull()
        }
    }

    override suspend fun deleteUser(id: Uuid) {
        mapExceptions {
            dbQuery {
                UserTable.deleteWhere { UserTable.id eq id.toJavaUuid() }
            }
            runCatching { cacheClient.deleteUser(id) }
        }
    }
}