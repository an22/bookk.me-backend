package com.book.auth.data.datasource

import com.book.auth.data.map.toDomain
import com.book.auth.data.orm.entity.AuthToHandleEntity
import com.book.auth.data.orm.entity.PasskeyCredentialEntity
import com.book.auth.data.orm.table.AuthToHandleTable
import com.book.auth.data.orm.table.AuthenticationTable
import com.book.auth.data.orm.table.PasskeyCredentialTable
import com.book.auth.domain.api.identification.entity.PasskeyCredential
import com.book.auth.domain.datasource.PassKeyDataSource
import com.book.core.data.DataSource
import com.book.core.data.cache.CacheClient
import com.book.core.data.cache.get
import com.book.core.data.cache.set
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
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
        mapExceptions {
            cacheClient.delete(requestId)
        }
    }

    override suspend fun createPasskeyCredential(credential: PasskeyCredential) {
        mapExceptions {
            transaction {
                PasskeyCredentialTable.insert {
                    it[identityId] = credential.userIdentityId
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
                    it[clientData] = credential.clientData
                    it[updatedAt] = Clock.System.now()
                }
            }
        }
    }

    override suspend fun getCredentialBy(userHandle: ByteArray, credentialId: ByteArray): PasskeyCredential? {
        return mapExceptions {
            transaction {
                PasskeyCredentialEntity.wrapRows(
                    AuthToHandleTable
                        .innerJoin(
                            otherTable = PasskeyCredentialTable,
                            onColumn = { id },
                            otherColumn = { identityId },
                            additionalConstraint = { PasskeyCredentialTable.credDescriptorId eq credentialId}
                        )
                        .select(PasskeyCredentialTable.columns)
                        .where { AuthToHandleTable.userHandle eq userHandle }
                )
                    .map(PasskeyCredentialEntity::toDomain)
                    .firstOrNull()
            }
        }
    }

    override suspend fun getEmailByHandle(userHandle: ByteArray): String? = mapExceptions {
        transaction {
            AuthToHandleEntity.find {
                AuthToHandleTable.userHandle eq userHandle
            }
                .map { it.authentication.email }
                .firstOrNull()
        }
    }

    override suspend fun getCredentialsByEmail(email: String): Set<PasskeyCredential> {
        return mapExceptions {
            transaction {
                PasskeyCredentialEntity.wrapRows(
                    AuthenticationTable
                        .innerJoin(
                            otherTable = AuthToHandleTable,
                            onColumn = { id },
                            otherColumn = { authId }
                        )
                        .innerJoin(
                            otherTable = PasskeyCredentialTable,
                            onColumn = { AuthToHandleTable.id },
                            otherColumn = { identityId }
                        )
                        .select(PasskeyCredentialTable.columns)
                        .where { AuthenticationTable.email eq email }
                )
                    .map(PasskeyCredentialEntity::toDomain)
                    .toSet()
            }
        }
    }

    override suspend fun getHandleByEmail(email: String): ByteArray? = mapExceptions {
        transaction {
            AuthToHandleEntity.find {
                AuthenticationTable.email eq email
            }
                .map { it.userHandle }
                .firstOrNull()
        }
    }

    override suspend fun savePasskeyHandle(authenticationId: Long, handle: ByteArray): Long {
        return mapExceptions {
            transaction {
                AuthToHandleTable.insertAndGetId {
                    it[authId] = authenticationId
                    it[userHandle] = handle
                }.value
            }
        }
    }
}