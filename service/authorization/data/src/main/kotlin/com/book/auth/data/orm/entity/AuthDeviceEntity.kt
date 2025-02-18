package com.book.auth.data.orm.entity

import com.book.auth.data.orm.table.AuthDeviceTable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

internal class AuthDeviceEntity(id: EntityID<Long>) : LongEntity(id) {

    var userAuth by AuthenticationEntity referencedOn AuthDeviceTable.userAuthId
    var deviceUUID by AuthDeviceTable.deviceUUID
    var deviceName by AuthDeviceTable.deviceName
    var refreshTokenId by AuthDeviceTable.refreshTokenId
    var isSignedIn by AuthDeviceTable.isSignedIn
    var createdAt by AuthDeviceTable.createdAt
    var updatedAt by AuthDeviceTable.updatedAt

    companion object : LongEntityClass<AuthDeviceEntity>(AuthDeviceTable)
}