package com.book.auth.data.orm

import org.ktorm.schema.Table
import org.ktorm.schema.boolean
import org.ktorm.schema.bytes
import org.ktorm.schema.long
import org.ktorm.schema.varchar

object UserHasAuthCredentials: Table<Nothing>("user_has_auth_credential") {
    val email = varchar("email").primaryKey()
    val userHandle = bytes("handle")
    val credDescriptorId = bytes("cred_descriptor_id")
    val credDescriptorType = varchar("cred_descriptor_type")
    val credDescriptorTransports = varchar("cred_descriptor_transports")
    val publicKey = bytes("public_key")
    val signatureCount = long("signature_count")
    val isDiscoverable = boolean("discoverable")
    val isBackupEligible = boolean("backup_eligible")
    val isBackedUp = boolean("backed_up")
    val attestationObject = bytes("attestation_object")
    val clientData = bytes("client_data")
}