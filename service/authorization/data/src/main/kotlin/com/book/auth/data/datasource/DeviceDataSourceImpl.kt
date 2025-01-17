package com.book.auth.data.datasource

import com.book.auth.data.map.toDomain
import com.book.auth.data.orm.entity.AuthDeviceEntity
import com.book.auth.data.orm.entity.AuthenticationEntity
import com.book.auth.data.orm.table.AuthDeviceTable
import com.book.auth.domain.api.entity.Device
import com.book.auth.domain.datasource.DeviceDataSource
import com.book.core.data.DataSource
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.transactions.transaction

internal class DeviceDataSourceImpl: DataSource(), DeviceDataSource {
    override suspend fun attachRefreshTokenToDevice(deviceId: Long, token: String) {
        transaction {
            AuthDeviceEntity.findByIdAndUpdate(deviceId) {
                it.isSignedIn = true
                it.refreshToken = token
                it.updatedAt = Clock.System.now()
            }
        }
    }

    override suspend fun getDeviceById(deviceId: Long): Device? = transaction {
        AuthDeviceEntity.find {
            AuthDeviceTable.id eq deviceId
        }
            .map(AuthDeviceEntity::toDomain)
            .firstOrNull()
    }

    override suspend fun getDeviceByUUID(deviceUUID: String): Device? = transaction {
        AuthDeviceEntity.find {
            AuthDeviceTable.deviceUUID eq deviceUUID
        }
            .map(AuthDeviceEntity::toDomain)
            .firstOrNull()
    }

    override suspend fun getDevices(authId: Long): List<Device> = transaction {
        AuthDeviceEntity.find {
            AuthDeviceTable.userAuthId eq authId
        }
            .map(AuthDeviceEntity::toDomain)
    }

    override suspend fun deleteTokenFromDevice(deviceId: Long) {
        transaction {
            AuthDeviceEntity.findByIdAndUpdate(deviceId) {
                it.isSignedIn = false
                it.refreshToken = null
                it.updatedAt = Clock.System.now()
            }
        }
    }

    override suspend fun createDevice(authId: Long, uuid: String, name: String): Device = transaction {
        AuthDeviceEntity.new {
            userAuth = AuthenticationEntity[authId]
            deviceUUID = uuid
            deviceName = name
            refreshToken = null
            isSignedIn = false
            updatedAt = Clock.System.now()
        }.toDomain()
    }
}