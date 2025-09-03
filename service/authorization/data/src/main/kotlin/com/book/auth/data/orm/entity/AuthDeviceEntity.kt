package com.book.auth.data.orm.entity

import com.book.auth.data.orm.table.AuthDeviceTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import java.util.UUID

internal class AuthDeviceEntity(id: EntityID<UUID>) : UUIDEntity(id) {

    var userAuth by AuthenticationEntity referencedOn AuthDeviceTable.userAuthId
    var deviceUUID by AuthDeviceTable.deviceUUID
    var deviceName by AuthDeviceTable.deviceName
    var refreshTokenId by AuthDeviceTable.refreshTokenId
    var isSignedIn by AuthDeviceTable.isSignedIn
    var createdAt by AuthDeviceTable.createdAt
    var updatedAt by AuthDeviceTable.updatedAt

    companion object : UUIDEntityClass<AuthDeviceEntity>(AuthDeviceTable)
}