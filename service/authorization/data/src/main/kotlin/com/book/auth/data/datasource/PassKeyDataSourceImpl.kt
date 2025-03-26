package com.book.auth.data.datasource

import com.book.auth.data.map.toDomain
import com.book.auth.data.orm.entity.PasskeyCredentialEntity
import com.book.auth.data.orm.table.AuthenticationTable
import com.book.auth.data.orm.table.PasskeyCredentialTable
import com.book.auth.domain.api.identification.entity.PasskeyCredential
import com.book.auth.domain.datasource.PassKeyDataSource
import com.book.core.data.DataSource
import com.book.core.data.cache.CacheClient
import com.book.core.data.cache.get
import com.book.core.data.cache.set
import com.bookk.core.toHexUUID
import com.bookk.core.toUUIDBytes
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import kotlin.time.Duration.Companion.minutes

internal class PassKeyDataSourceImpl(
    private val cacheClient: CacheClient<String>
) : DataSource(), PassKeyDataSource {

    override suspend fun saveChallengeToCache(requestId: String, challenge: String) = mapExceptions {
        cacheClient.withTransaction {
            set(requestId, challenge)
            setExpiration(requestId, 5.minutes)
        }
    }

    override suspend fun getCachedChallenge(requestId: String): String? {
        return mapExceptions { cacheClient.get(requestId) }
    }

    override suspend fun deleteCachedChallenge(requestId: String) {
        mapExceptions { cacheClient.delete(requestId) }
    }

    override suspend fun createPasskeyCredential(credential: PasskeyCredential) {
        mapExceptions {
            transaction {
                PasskeyCredentialTable.insert {
                    it[authUUID] = credential.handle
                    it[credDescriptorId] = credential.credDescriptor.id
                    it[credDescriptorType] = credential.credDescriptor.type
                    it[credDescriptorTransports] =
                        credential.credDescriptor.transports.joinToString(separator = ",") { it }
                    it[publicKey] = credential.publicKey
                    it[signatureCount] = credential.signatureCount
                    it[isDiscoverable] = credential.isDiscoverable
                    it[isBackupEligible] = credential.isBackupEligible
                    it[isBackedUp] = credential.isBackedUp
                    it[attestationObject] = credential.attestationObject
                    it[clientDataJson] = credential.clientData
                    it[updatedAt] = Clock.System.now()
                }
            }
        }
    }

    override suspend fun getCredentialBy(userHandle: ByteArray, credentialId: ByteArray): PasskeyCredential? {
        return mapExceptions {
            transaction {
                PasskeyCredentialEntity
                    .find {
                        (PasskeyCredentialTable.authUUID eq userHandle.toHexUUID()) and
                            (PasskeyCredentialTable.credDescriptorId eq credentialId)
                    }
                    .map(PasskeyCredentialEntity::toDomain)
                    .firstOrNull()
            }
        }
    }

    override suspend fun getCredentialBy(authId: Long): List<PasskeyCredential> {
        return mapExceptions {
            transaction {
                PasskeyCredentialEntity.wrapRows(
                    AuthenticationTable
                        .innerJoin(
                            otherTable = PasskeyCredentialTable,
                            onColumn = { uuid },
                            otherColumn = { authUUID }
                        )
                        .select(PasskeyCredentialTable.columns)
                        .where { AuthenticationTable.id eq authId }
                ).map(PasskeyCredentialEntity::toDomain)
            }
        }
    }

    override suspend fun getUsernameByHandle(userHandle: ByteArray): String? = mapExceptions {
        transaction {
            val strRepresentation = userHandle.toHexUUID()
            val exists = AuthenticationTable
                .select(AuthenticationTable.id)
                .where { AuthenticationTable.uuid eq strRepresentation }
                .empty()

            strRepresentation.takeIf { exists }
        }
    }

    override suspend fun getCredentialsByUsername(username: String): Set<PasskeyCredential> {
        return mapExceptions {
            transaction {
                PasskeyCredentialEntity
                    .find { PasskeyCredentialTable.authUUID eq username }
                    .map(PasskeyCredentialEntity::toDomain)
                    .toSet()
            }
        }
    }

    override suspend fun getCredentialsByCredentialId(credentialId: ByteArray): Set<PasskeyCredential> {
        return mapExceptions {
            transaction {
                PasskeyCredentialEntity.find {
                    PasskeyCredentialTable.credDescriptorId eq credentialId
                }
                    .map(PasskeyCredentialEntity::toDomain)
                    .toSet()
            }
        }
    }

    override suspend fun getHandleByUsername(username: String): ByteArray? = mapExceptions {
        transaction {
            val exists = AuthenticationTable
                .select(AuthenticationTable.id)
                .where { AuthenticationTable.uuid eq username }
                .empty()

            username.toUUIDBytes().takeIf { exists }
        }
    }

    override suspend fun markAsUsed(passkeyCredentialId: Long) {
        mapExceptions {
            transaction {
                PasskeyCredentialTable.update(where = { PasskeyCredentialTable.id eq passkeyCredentialId }) {
                    it[lastUsedAt] = Clock.System.now()
                }
            }
        }
    }
}