package com.bookk.auth.data.datasource

import com.bookk.auth.data.map.toDomain
import com.bookk.auth.data.orm.entity.AuthDeviceEntity
import com.bookk.auth.data.orm.table.AuthDeviceTable
import com.bookk.auth.data.orm.table.AuthenticationTable
import com.bookk.auth.domain.api.identification.entity.Device
import com.bookk.auth.domain.datasource.DeviceDataSource
import com.bookk.core.data.DataSource
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.innerJoin
import org.jetbrains.exposed.v1.jdbc.insertIgnore
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.time.Clock
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

internal class DeviceDataSourceImpl : DataSource(), DeviceDataSource {
    override suspend fun attachRefreshTokenToDevice(deviceId: Uuid, tokenId: Uuid) = dbQuery<Unit> {
        AuthDeviceTable.update(where = { AuthDeviceTable.id eq deviceId.toJavaUuid() }) {
            it[isSignedIn] = true
            it[refreshTokenId] = tokenId.toJavaUuid()
            it[updatedAt] = Clock.System.now()
        }
    }

    override suspend fun getDeviceById(deviceId: Uuid): Device? = dbQuery {
        AuthDeviceTable
            .innerJoin(AuthenticationTable, onColumn = { userAuthId }, otherColumn = { id })
            .selectAll()
            .where { AuthDeviceTable.id eq deviceId.toJavaUuid() }
            .map { AuthDeviceEntity.wrapRow(it).toDomain() }
            .singleOrNull()
    }

    override suspend fun getDeviceByAuthIdAndUUID(authId: Uuid, deviceUUID: Uuid): Device? = dbQuery {
        AuthDeviceTable
            .innerJoin(
                otherTable = AuthenticationTable,
                onColumn = { userAuthId },
                otherColumn = { id },
                additionalConstraint = { AuthenticationTable.id eq authId.toJavaUuid() }
            )
            .selectAll()
            .where { AuthDeviceTable.deviceUUID eq deviceUUID.toJavaUuid() }
            .map { AuthDeviceEntity.wrapRow(it).toDomain() }
            .singleOrNull()
    }

    override suspend fun getDevices(authId: Uuid): List<Device> = dbQuery {
        AuthDeviceTable
            .innerJoin(
                otherTable = AuthenticationTable,
                onColumn = { userAuthId },
                otherColumn = { id },
                additionalConstraint = { AuthDeviceTable.userAuthId eq authId.toJavaUuid() }
            )
            .selectAll()
            .map { AuthDeviceEntity.wrapRow(it).toDomain() }
            .toList()
    }

    override suspend fun deleteTokenFromDevice(deviceId: Uuid) = dbQuery<Unit> {
        AuthDeviceTable.update(
            where = { AuthDeviceTable.id eq deviceId.toJavaUuid() }
        ) {
            it[isSignedIn] = false
            it[refreshTokenId] = null
            it[updatedAt] = Clock.System.now()
        }
    }

    override suspend fun createDeviceIfNotExist(authId: Uuid, uuid: Uuid, name: String) = dbQuery<Unit> {
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