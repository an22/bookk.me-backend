package com.book.auth.data.datasource

import com.book.auth.data.map.toDomain
import com.book.auth.data.orm.entity.AuthenticationEntity
import com.book.auth.data.orm.table.AuthenticationTable
import com.book.auth.domain.api.authentication.entity.Authentication
import com.book.auth.domain.datasource.AccountDataSource
import com.book.core.data.DataSource
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insertAndGetId
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import kotlin.time.Clock
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

internal class AccountDataSourceImpl : DataSource(), AccountDataSource {

    override suspend fun createAuthorization(info: Authentication): Authentication = mapExceptions {
        suspendTransaction {
            AuthenticationTable.insertAndGetId {
                it[userId] = info.userId.toJavaUuid()
                it[uuid] = info.uuid.toJavaUuid()
                it[updatedAt] = Clock.System.now()
            }.let {
                info.copy(id = it.value.toKotlinUuid())
            }
        }
    }

    override suspend fun getAuthRecordById(id: Uuid): Authentication? = mapExceptions {
        suspendTransaction {
            AuthenticationTable.selectAll()
                .where { AuthenticationTable.id eq id.toJavaUuid() }
                .map { AuthenticationEntity.wrapRow(it).toDomain() }
                .firstOrNull()
        }
    }

    override suspend fun getAuthRecordByUUID(uuid: Uuid): Authentication? = mapExceptions {
        suspendTransaction {
            AuthenticationTable.selectAll()
                .where  { AuthenticationTable.uuid eq uuid.toJavaUuid() }
                .map { AuthenticationEntity.wrapRow(it).toDomain() }
                .firstOrNull()
        }
    }

    override suspend fun getAuthRecordByUserId(userId: Uuid): Authentication? = mapExceptions {
        suspendTransaction {
            AuthenticationTable.selectAll()
                .where  { AuthenticationTable.userId eq userId.toJavaUuid() }
                .map { AuthenticationEntity.wrapRow(it).toDomain() }
                .firstOrNull()
        }
    }

    override suspend fun deleteAuthorization(authId: Uuid) {
        mapExceptions {
            suspendTransaction {
                AuthenticationTable.deleteWhere {
                    AuthenticationTable.id eq authId.toJavaUuid()
                }
            }
        }
    }
}