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
import com.book.user.domain.api.entity.UserEditModel
import com.book.user.domain.datasource.UserDataSource
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import org.jetbrains.exposed.v1.core.statements.UpsertSqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insertAndGetId
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.r2dbc.update
import kotlin.time.Clock
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

internal class UserDataSourceImpl(
    private val cacheClient: CacheClient<String>
) : DataSource(), UserDataSource {

    override suspend fun insertNewUser(user: User) = mapExceptions {
        val createdUser = suspendTransaction {
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
            val updatedRowCount = suspendTransaction {
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
        val user = suspendTransaction {
            UserTable.selectAll()
                .where { UserTable.id eq id.toJavaUuid() }
                .map { UserEntity.wrapRow(it).toDomain() }
                .firstOrNull()
        }
        if (user != null) {
            cacheClient.save(user)
        }
        return@mapExceptions user
    }

    override suspend fun getUserByEmail(email: String): User? = mapExceptions {
        suspendTransaction {
            UserTable.selectAll()
                .where { UserTable.email eq email }
                .map { UserEntity.wrapRow(it).toDomain() }
                .firstOrNull()
        }
    }

    override suspend fun deleteUser(id: Uuid) {
        mapExceptions {
            suspendTransaction {
                UserTable.deleteWhere { UserTable.id eq id.toJavaUuid() }
            }
            runCatching { cacheClient.deleteUser(id) }
        }
    }
}