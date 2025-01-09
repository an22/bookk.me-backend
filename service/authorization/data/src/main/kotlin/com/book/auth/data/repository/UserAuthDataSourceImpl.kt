package com.book.auth.data.repository

import com.book.auth.data.map.toDomain
import com.book.auth.data.orm.entity.AuthDeviceEntity
import com.book.auth.data.orm.entity.AuthenticationEntity
import com.book.auth.data.orm.table.AuthDeviceTable
import com.book.auth.data.orm.table.AuthenticationTable
import com.book.auth.domain.api.datasource.UserAuthDataSource
import com.book.auth.domain.api.entity.Authentication
import com.book.auth.domain.api.entity.Device
import com.book.core.data.DataSource
import com.book.core.data.cache.CacheClient

internal class UserAuthDataSourceImpl(
    private val cacheClient: CacheClient<String>
) : DataSource(), UserAuthDataSource {

    override suspend fun createAuthorization(info: Authentication): Authentication = dbTransaction {
        AuthenticationEntity.new {
            userId = info.userId
            email = info.email
        }.toDomain()
    }

    override suspend fun getAuthRecordById(id: Long): Authentication? = dbTransaction {
        AuthenticationEntity[id].toDomain()
    }

    override suspend fun getAuthRecordByEmail(email: String): Authentication? = dbTransaction {
        AuthenticationEntity.find {
            AuthenticationTable.email eq email
        }
            .map(AuthenticationEntity::toDomain)
            .firstOrNull()
    }

    override suspend fun getAuthRecordByUserId(userId: Long): Authentication? = dbTransaction {
        AuthenticationEntity.find {
            AuthenticationTable.userId eq userId
        }
            .map(AuthenticationEntity::toDomain)
            .firstOrNull()
    }

    override suspend fun deleteAuthorization(authId: Long) {
        dbTransaction {
            AuthenticationEntity[authId].delete()
        }
    }

    override suspend fun saveUserRefreshToken(deviceId: Long, token: String) {
        dbTransaction {
            AuthDeviceEntity.findByIdAndUpdate(deviceId) {
                it.isSignedIn = true
                it.refreshToken = token
            }
        }
    }

    override suspend fun getDeviceById(deviceId: Long): Device? = dbTransaction {
        AuthDeviceEntity.find {
            AuthDeviceTable.id eq deviceId
        }
            .map(AuthDeviceEntity::toDomain)
            .firstOrNull()
    }

    override suspend fun getDeviceByUUID(deviceUUID: String): Device? = dbTransaction {
        AuthDeviceEntity.find {
            AuthDeviceTable.deviceUUID eq deviceUUID
        }
            .map(AuthDeviceEntity::toDomain)
            .firstOrNull()
    }

    override suspend fun getDevices(authId: Long): List<Device> = dbTransaction {
        AuthDeviceEntity.find {
            AuthDeviceTable.userAuthId eq authId
        }
            .map(AuthDeviceEntity::toDomain)
    }

    override suspend fun deleteTokenInfoForDevice(deviceId: Long) {
        dbTransaction {
            AuthDeviceEntity.findByIdAndUpdate(deviceId) {
                it.isSignedIn = false
                it.refreshToken = null
            }
        }
    }

    override suspend fun createDevice(authId: Long, uuid: String, name: String): Device = dbTransaction {
        AuthDeviceEntity.new {
            userAuth = AuthenticationEntity[authId]
            deviceUUID = uuid
            deviceName = name
            refreshToken = null
            isSignedIn = false
        }.toDomain()
    }
}