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
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.time.Clock
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

internal class UserDataSourceImpl(
    private val cacheClient: CacheClient<String>
) : DataSource(), UserDataSource {

    override suspend fun insertNewUser(user: User) = dbQuery {
        UserTable.insertAndGetId {
            it[name] = user.name
            it[lastName] = user.lastName
            it[email] = user.email
            it[updatedAt] = Clock.System.now()
        }.let {
            user.copy(id = it.value.toKotlinUuid())
        }.also {
            runCatching { cacheClient.save(it) }
        }
    }

    override suspend fun updateUser(id: Uuid, user: UserEditModel): Boolean = dbQuery {
        val updatedRowCount = UserTable.update(where = { UserTable.id eq id.toJavaUuid() }) {
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
        val isUpdated = updatedRowCount > 0
        if (isUpdated) {
            cacheClient.deleteUser(id)
        }
        isUpdated
    }

    override suspend fun getUserById(id: Uuid): User? = dbQuery {
        val cached: User? = cacheClient.getUser(id)
        if (cached != null) return@dbQuery cached
        UserTable.selectAll()
            .where { UserTable.id eq id.toJavaUuid() }
            .map { UserEntity.wrapRow(it).toDomain() }
            .singleOrNull()
            ?.also { user ->
                runCatching { cacheClient.save(user) }
                    .onFailure { cacheClient.deleteUser(user.id) }
            }
    }

    override suspend fun getUserByEmail(email: String): User? = dbQuery {
        UserTable.selectAll()
            .where { UserTable.email eq email }
            .map { UserEntity.wrapRow(it).toDomain() }
            .singleOrNull()
    }

    override suspend fun deleteUser(id: Uuid) = dbQuery<Unit> {
        UserTable.deleteWhere { UserTable.id eq id.toJavaUuid() }
        runCatching { cacheClient.deleteUser(id) }
    }
}