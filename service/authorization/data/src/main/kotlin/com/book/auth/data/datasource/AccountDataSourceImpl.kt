package com.book.auth.data.datasource

import com.book.auth.data.map.toDomain
import com.book.auth.data.orm.entity.AuthenticationEntity
import com.book.auth.data.orm.table.AuthenticationTable
import com.book.auth.domain.api.entity.Authentication
import com.book.auth.domain.datasource.AccountDataSource
import com.book.core.data.DataSource
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.transactions.transaction

internal class AccountDataSourceImpl : DataSource(), AccountDataSource {

    override suspend fun createAuthorization(info: Authentication): Authentication = transaction {
        AuthenticationEntity.new {
            userId = info.userId
            email = info.email
            updatedAt = Clock.System.now()
        }.toDomain()
    }

    override suspend fun getAuthRecordById(id: Long): Authentication? = transaction {
        AuthenticationEntity[id].toDomain()
    }

    override suspend fun getAuthRecordByEmail(email: String): Authentication? = transaction {
        AuthenticationEntity.find {
            AuthenticationTable.email eq email
        }
            .map(AuthenticationEntity::toDomain)
            .firstOrNull()
    }

    override suspend fun getAuthRecordByUserId(userId: Long): Authentication? = transaction {
        AuthenticationEntity.find {
            AuthenticationTable.userId eq userId
        }
            .map(AuthenticationEntity::toDomain)
            .firstOrNull()
    }

    override suspend fun deleteAuthorization(authId: Long) {
        transaction {
            AuthenticationEntity[authId].delete()
        }
    }
}