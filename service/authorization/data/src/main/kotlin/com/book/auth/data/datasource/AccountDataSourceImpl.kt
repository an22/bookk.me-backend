package com.book.auth.data.datasource

import com.book.auth.data.map.toDomain
import com.book.auth.data.orm.entity.AuthenticationEntity
import com.book.auth.data.orm.table.AuthenticationTable
import com.book.auth.domain.api.authentication.entity.Authentication
import com.book.auth.domain.datasource.AccountDataSource
import com.book.core.data.DataSource
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.transactions.transaction

internal class AccountDataSourceImpl : DataSource(), AccountDataSource {

    override suspend fun createAuthorization(info: Authentication): Authentication = mapExceptions {
        transaction {
            AuthenticationEntity.new {
                userId = info.userId
                uuid = info.uuid
                updatedAt = Clock.System.now()
            }.toDomain()
        }
    }

    override suspend fun getAuthRecordById(id: Long): Authentication? = mapExceptions {
        transaction {
            AuthenticationEntity[id].toDomain()
        }
    }

    override suspend fun getAuthRecordByUUID(uuid: String): Authentication? = mapExceptions {
        transaction {
            AuthenticationEntity.find {
                AuthenticationTable.uuid eq uuid
            }
                .map(AuthenticationEntity::toDomain)
                .firstOrNull()
        }
    }

    override suspend fun getAuthRecordByUserId(userId: Long): Authentication? = mapExceptions {
        transaction {
            AuthenticationEntity.find {
                AuthenticationTable.userId eq userId
            }
                .map(AuthenticationEntity::toDomain)
                .firstOrNull()
        }
    }

    override suspend fun deleteAuthorization(authId: Long) {
        mapExceptions {
            transaction {
                AuthenticationEntity[authId].delete()
            }
        }
    }
}