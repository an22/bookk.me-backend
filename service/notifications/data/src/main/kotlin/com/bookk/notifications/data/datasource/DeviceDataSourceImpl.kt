package com.bookk.notifications.data.datasource

import com.bookk.core.data.DataSource
import com.bookk.core.domain.entity.Error
import com.bookk.notifications.data.orm.entity.DeviceEntity
import com.bookk.notifications.data.orm.table.DeviceTable
import com.bookk.notifications.domain.api.entity.Device
import com.bookk.notifications.domain.datasource.DeviceDataSource
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

internal class DeviceDataSourceImpl : DataSource(), DeviceDataSource {

    override suspend fun create(authId: Uuid, deviceUUID: Uuid, userId: Uuid): Device = dbQuery {
        DeviceEntity.new {
            this.authId = authId.toJavaUuid()
            this.deviceUUID = deviceUUID.toJavaUuid()
            this.userId = userId.toJavaUuid()
            this.notificationToken = null
        }.domain()
    }

    override suspend fun getById(id: Uuid): Device? = dbQuery {
        DeviceEntity.findById(id.toJavaUuid())?.domain()
    }

    override suspend fun getByDeviceUuid(deviceUuid: Uuid): Device? = dbQuery {
        DeviceEntity
            .find { DeviceTable.deviceUuid eq deviceUuid.toJavaUuid() }
            .firstOrNull()
            ?.domain()
    }

    override suspend fun getByAuthId(authId: Uuid): Device? = dbQuery {
        DeviceEntity
            .find { DeviceTable.authId eq authId.toJavaUuid() }
            .firstOrNull()
            ?.domain()
    }

    override suspend fun getByUserId(userId: Uuid): List<Device> = dbQuery {
        DeviceEntity
            .find { DeviceTable.userId eq userId.toJavaUuid() }
            .map { it.domain() }
    }

    override suspend fun updateToken(deviceUuid: Uuid, token: String?): Device = dbQuery {
        DeviceEntity
            .find { DeviceTable.deviceUuid eq deviceUuid.toJavaUuid() }
            .firstOrNull()
            ?.also { it.notificationToken = token }
            ?.domain()
            ?: throw Error.NotFound()
    }

    override suspend fun deleteByDeviceUuid(deviceUuid: Uuid) = dbQuery<Unit> {
        DeviceTable.deleteWhere { DeviceTable.deviceUuid eq deviceUuid.toJavaUuid() }
    }
}
