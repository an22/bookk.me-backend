package com.book.auth.data.datasource

import com.book.auth.data.map.toDomain
import com.book.auth.data.orm.entity.AuthDeviceEntity
import com.book.auth.data.orm.table.AuthDeviceTable
import com.book.auth.domain.api.identification.entity.Device
import com.book.auth.domain.datasource.DeviceDataSource
import com.book.core.data.DataSource
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.transactions.transaction

internal class DeviceDataSourceImpl : DataSource(), DeviceDataSource {
    override suspend fun attachRefreshTokenToDevice(deviceId: Long, token: String) {
        mapExceptions {
            transaction {
                AuthDeviceEntity.findByIdAndUpdate(deviceId) {
                    it.isSignedIn = true
                    it.refreshToken = token
                    it.updatedAt = Clock.System.now()
                }
            }
        }
    }

    override suspend fun getDeviceById(deviceId: Long): Device? = mapExceptions {
        transaction {
            AuthDeviceEntity.find {
                AuthDeviceTable.id eq deviceId
            }
                .map(AuthDeviceEntity::toDomain)
                .firstOrNull()
        }
    }

    override suspend fun getDeviceByAuthIdAndUUID(authId: Long, deviceUUID: String): Device? = mapExceptions {
        transaction {
            AuthDeviceEntity.find {
                (AuthDeviceTable.deviceUUID eq deviceUUID) and (AuthDeviceTable.userAuthId eq authId)
            }
                .map(AuthDeviceEntity::toDomain)
                .firstOrNull()
        }
    }

    override suspend fun getDevices(authId: Long): List<Device> = mapExceptions {
        transaction {
            AuthDeviceEntity.find {
                AuthDeviceTable.userAuthId eq authId
            }
                .map(AuthDeviceEntity::toDomain)
        }
    }

    override suspend fun deleteTokenFromDevice(deviceId: Long) {
        mapExceptions {
            transaction {
                AuthDeviceEntity.findByIdAndUpdate(deviceId) {
                    it.isSignedIn = false
                    it.refreshToken = null
                    it.updatedAt = Clock.System.now()
                }
            }
        }
    }

    override suspend fun createDeviceIfNotExist(authId: Long, uuid: String, name: String) {
        mapExceptions {
            transaction {
                AuthDeviceTable.insertIgnore {
                    it[userAuthId] = authId
                    it[deviceUUID] = uuid
                    it[deviceName] = name
                    it[refreshToken] = null
                    it[isSignedIn] = false
                    it[updatedAt] = Clock.System.now()
                }
            }
        }
    }
}