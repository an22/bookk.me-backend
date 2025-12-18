package com.bookk.auth.data.orm.entity

import com.bookk.auth.data.orm.table.PasskeyCredentialTable
import com.bookk.core.data.R2dbcUUIDEntityClass
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UUIDEntity
import java.util.UUID

internal class PasskeyCredentialEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    var authorization by AuthenticationEntity referencedOn PasskeyCredentialTable.authId
    var credDescriptorId by PasskeyCredentialTable.credDescriptorId
    var credDescriptorType by PasskeyCredentialTable.credDescriptorType
    var credDescriptorTransports by PasskeyCredentialTable.credDescriptorTransports
    var name by PasskeyCredentialTable.name
    var publicKey by PasskeyCredentialTable.publicKey
    var signatureCount by PasskeyCredentialTable.signatureCount
    var isDiscoverable by PasskeyCredentialTable.isDiscoverable
    var isBackupEligible by PasskeyCredentialTable.isBackupEligible
    var isBackedUp by PasskeyCredentialTable.isBackedUp
    var attestationObject by PasskeyCredentialTable.attestationObject
    var clientData by PasskeyCredentialTable.clientDataJson
    var createdAt by PasskeyCredentialTable.createdAt
    var updatedAt by PasskeyCredentialTable.updatedAt
    var lastUsedAt by PasskeyCredentialTable.lastUsedAt

    companion object : R2dbcUUIDEntityClass<PasskeyCredentialEntity>(PasskeyCredentialTable)
}