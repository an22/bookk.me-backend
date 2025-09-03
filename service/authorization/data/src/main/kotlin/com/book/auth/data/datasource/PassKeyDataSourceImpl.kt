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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.toSet
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.greater
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.count
import org.jetbrains.exposed.v1.core.innerJoin
import org.jetbrains.exposed.v1.core.longLiteral
import org.jetbrains.exposed.v1.core.wrapAsExpression
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.r2dbc.update
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

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
            suspendTransaction {
                PasskeyCredentialTable.insert {
                    it[authUUID] = credential.handle.toJavaUuid()
                    it[name] = credential.name
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

    override suspend fun getCredentialBy(userHandle: Uuid, credentialId: ByteArray): PasskeyCredential? {
        return mapExceptions {
            suspendTransaction {
                PasskeyCredentialTable
                    .innerJoin(
                        AuthenticationTable,
                        onColumn = { authUUID },
                        otherColumn = { id },
                        additionalConstraint = { PasskeyCredentialTable.authUUID eq userHandle.toJavaUuid() }
                    )
                    .selectAll()
                    .map { PasskeyCredentialEntity.wrapRow(it).toDomain() }
                    .firstOrNull()
            }
        }
    }

    override suspend fun getCredentialBy(authId: Uuid): List<PasskeyCredential> {
        return mapExceptions {
            suspendTransaction {
                AuthenticationTable
                    .innerJoin(
                        otherTable = PasskeyCredentialTable,
                        onColumn = { uuid },
                        otherColumn = { authUUID },
                    )
                    .select(PasskeyCredentialTable.columns)
                    .where { AuthenticationTable.id eq authId.toJavaUuid() }
                    .map { PasskeyCredentialEntity.wrapRow(it).toDomain() }
                    .toList()
            }
        }
    }

    override suspend fun getUsernameByHandle(userHandle: Uuid): String? = mapExceptions {
        suspendTransaction {
            val exists = AuthenticationTable
                .select(AuthenticationTable.id)
                .where { AuthenticationTable.uuid eq userHandle.toJavaUuid() }
                .empty()
                .not()

            userHandle.toString().takeIf { exists }
        }
    }

    override suspend fun getCredentialsByUsername(username: Uuid): Set<PasskeyCredential> {
        return mapExceptions {
            suspendTransaction {
                PasskeyCredentialTable
                    .innerJoin(
                        otherTable = PasskeyCredentialTable,
                        onColumn = { AuthenticationTable.uuid },
                        otherColumn = { authUUID },
                        additionalConstraint = { PasskeyCredentialTable.authUUID eq username.toJavaUuid() }
                    )
                    .selectAll()
                    .map { PasskeyCredentialEntity.wrapRow(it).toDomain() }
                    .toSet()
            }
        }
    }

    override suspend fun getCredentialsByCredentialId(credentialId: ByteArray): Set<PasskeyCredential> {
        return mapExceptions {
            suspendTransaction {
                PasskeyCredentialTable
                    .innerJoin(
                        otherTable = PasskeyCredentialTable,
                        onColumn = { AuthenticationTable.uuid },
                        otherColumn = { authUUID },
                    )
                    .selectAll()
                    .where { PasskeyCredentialTable.credDescriptorId eq credentialId }
                    .map { PasskeyCredentialEntity.wrapRow(it).toDomain() }
                    .toSet()
            }
        }
    }

    override suspend fun getHandleByUsername(username: Uuid): Uuid? = mapExceptions {
        suspendTransaction {
            val exists = AuthenticationTable
                .select(AuthenticationTable.id)
                .where { AuthenticationTable.uuid eq username.toJavaUuid() }
                .empty()
                .not()

            username.takeIf { exists }
        }
    }

    override suspend fun markAsUsed(passkeyCredentialId: Uuid) {
        mapExceptions {
            suspendTransaction {
                PasskeyCredentialTable.update(where = { PasskeyCredentialTable.id eq passkeyCredentialId.toJavaUuid() }) {
                    it[lastUsedAt] = Clock.System.now()
                }
            }
        }
    }

    override suspend fun deletePasskey(id: Uuid, authId: Uuid): Int {
        return mapExceptions {
            suspendTransaction {
                val existingPasskeys = AuthenticationTable
                    .innerJoin(
                        otherTable = PasskeyCredentialTable,
                        onColumn = { uuid },
                        otherColumn = { authUUID }
                    )
                    .select(PasskeyCredentialTable.id.count())
                    .where { AuthenticationTable.id eq authId.toJavaUuid() }
                    .let { wrapAsExpression<Long>(it) }
                PasskeyCredentialTable.deleteWhere {
                    (PasskeyCredentialTable.id eq id.toJavaUuid()) and (existingPasskeys greater longLiteral(1L))
                }
            }
        }
    }
}