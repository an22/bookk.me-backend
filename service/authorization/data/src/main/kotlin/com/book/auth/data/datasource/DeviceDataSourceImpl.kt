package com.book.auth.data.datasource

import com.book.auth.data.map.toDomain
import com.book.auth.data.orm.entity.AuthDeviceEntity
import com.book.auth.data.orm.table.AuthDeviceTable
import com.book.auth.data.orm.table.AuthenticationTable
import com.book.auth.domain.api.identification.entity.Device
import com.book.auth.domain.datasource.DeviceDataSource
import com.book.core.data.DataSource
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.innerJoin
import org.jetbrains.exposed.v1.r2dbc.insertIgnore
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.update
import kotlin.time.Clock
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

internal class DeviceDataSourceImpl : DataSource(), DeviceDataSource {
    override suspend fun attachRefreshTokenToDevice(deviceId: Uuid, tokenId: Uuid) {
        mapExceptions {
            dbQuery {
                AuthDeviceTable.update(where = { AuthDeviceTable.id eq deviceId.toJavaUuid() }) {
                    it[isSignedIn] = true
                    it[refreshTokenId] = tokenId.toJavaUuid()
                    it[updatedAt] = Clock.System.now()
                }
            }
        }
    }

    override suspend fun getDeviceById(deviceId: Uuid): Device? = mapExceptions {
        dbQuery {
            AuthDeviceTable
                .innerJoin(AuthenticationTable, onColumn = { userAuthId }, otherColumn = { id })
                .selectAll()
                .map { AuthDeviceEntity.wrapRowR2dbc(it).toDomain() }
                .singleOrNull()
        }
    }

    override suspend fun getDeviceByAuthIdAndUUID(authId: Uuid, deviceUUID: Uuid): Device? = mapExceptions {
        dbQuery {
            AuthDeviceTable
                .innerJoin(
                    otherTable = AuthenticationTable,
                    onColumn = { userAuthId },
                    otherColumn = { id },
                    additionalConstraint = { AuthenticationTable.id eq authId.toJavaUuid() }
                )
                .selectAll()
                .where { AuthDeviceTable.deviceUUID eq deviceUUID.toJavaUuid() }
                .map { AuthDeviceEntity.wrapRowR2dbc(it).toDomain() }
                .singleOrNull()
        }
    }

    override suspend fun getDevices(authId: Uuid): List<Device> = mapExceptions {
        dbQuery {
            AuthDeviceTable
                .innerJoin(
                    otherTable = AuthenticationTable,
                    onColumn = { userAuthId },
                    otherColumn = { id },
                    additionalConstraint = { AuthDeviceTable.userAuthId eq authId.toJavaUuid() }
                )
                .selectAll()
                .map { AuthDeviceEntity.wrapRowR2dbc(it).toDomain() }
                .toList()
        }
    }

    override suspend fun deleteTokenFromDevice(deviceId: Uuid) {
        mapExceptions {
            dbQuery {
                AuthDeviceTable.update(
                    where = { AuthDeviceTable.id eq deviceId.toJavaUuid() }
                ) {
                    it[isSignedIn] = false
                    it[refreshTokenId] = null
                    it[updatedAt] = Clock.System.now()
                }
            }
        }
    }

    override suspend fun createDeviceIfNotExist(authId: Uuid, uuid: Uuid, name: String) {
        mapExceptions {
            AuthDeviceTable.insertIgnore {
                it[userAuthId] = authId.toJavaUuid()
                it[deviceUUID] = uuid.toJavaUuid()
                it[deviceName] = name
                it[refreshTokenId] = null
                it[isSignedIn] = false
                it[updatedAt] = Clock.System.now()
            }
        }
    }
}