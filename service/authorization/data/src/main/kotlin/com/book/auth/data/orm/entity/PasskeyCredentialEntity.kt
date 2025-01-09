package com.book.auth.data.orm.entity

import com.book.auth.data.orm.table.PasskeyCredentialTable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

internal class PasskeyCredentialEntity(id: EntityID<Long>) : LongEntity(id) {
    var authId by AuthenticationEntity referencedOn PasskeyCredentialTable.authId
    var userHandle by PasskeyCredentialTable.userHandle
    var credDescriptorId by PasskeyCredentialTable.credDescriptorId
    var credDescriptorType by PasskeyCredentialTable.credDescriptorType
    var credDescriptorTransports by PasskeyCredentialTable.credDescriptorTransports
    var publicKey by PasskeyCredentialTable.publicKey
    var signatureCount by PasskeyCredentialTable.signatureCount
    var isDiscoverable by PasskeyCredentialTable.isDiscoverable
    var isBackupEligible by PasskeyCredentialTable.isBackupEligible
    var isBackedUp by PasskeyCredentialTable.isBackedUp
    var attestationObject by PasskeyCredentialTable.attestationObject
    var clientData by PasskeyCredentialTable.clientData
    var createdAt by PasskeyCredentialTable.createdAt
    var updatedAt by PasskeyCredentialTable.updatedAt

    companion object : LongEntityClass<PasskeyCredentialEntity>(PasskeyCredentialTable)
}