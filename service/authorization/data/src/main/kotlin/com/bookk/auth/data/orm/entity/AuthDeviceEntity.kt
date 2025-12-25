package com.bookk.auth.data.orm.entity

import com.bookk.auth.data.orm.table.AuthDeviceTable
import com.bookk.core.data.DecoratorUUIDEntityClass
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UUIDEntity
import java.util.UUID

internal class AuthDeviceEntity(id: EntityID<UUID>) : UUIDEntity(id) {

    var userAuth by AuthenticationEntity referencedOn AuthDeviceTable.userAuthId
    var deviceUUID by AuthDeviceTable.deviceUUID
    var deviceName by AuthDeviceTable.deviceName
    var refreshTokenId by AuthDeviceTable.refreshTokenId
    var isSignedIn by AuthDeviceTable.isSignedIn
    var createdAt by AuthDeviceTable.createdAt
    var updatedAt by AuthDeviceTable.updatedAt

    companion object : DecoratorUUIDEntityClass<AuthDeviceEntity>(AuthDeviceTable)
}