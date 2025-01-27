package com.book.auth.data.orm.table

import kotlinx.datetime.Clock
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

internal object PasskeyCredentialTable: LongIdTable("passkey_credentials") {
    val identityId = reference("identity_id", AuthToHandleTable, onDelete = ReferenceOption.CASCADE)
    val credDescriptorId = binary("cred_descriptor_id", 24)
    val credDescriptorType = varchar("cred_descriptor_type", 255)
    val credDescriptorTransports = varchar("cred_descriptor_transports", 255)
    val publicKey = binary("public_key", 255)
    val signatureCount = long("signature_count")
    val isDiscoverable = bool("discoverable")
    val isBackupEligible = bool("backup_eligible")
    val isBackedUp = bool("backed_up")
    val attestationObject = binary("attestation_object", 255)
    val clientData = binary("client_data", 255)
    val createdAt = timestamp("created_at").clientDefault { Clock.System.now() }
    val updatedAt = timestamp("updated_at")

    init {
        uniqueIndex(identityId, credDescriptorId)
    }
}