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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.toSet
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.count
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.innerJoin
import org.jetbrains.exposed.v1.core.longLiteral
import org.jetbrains.exposed.v1.core.wrapAsExpression
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.selectAll
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
            PasskeyCredentialTable.insert {
                it[authId] = credential.authId.toJavaUuid()
                it[name] = credential.name
                it[credDescriptorId] = credential.credDescriptor.id
                it[credDescriptorType] = credential.credDescriptor.type
                it[credDescriptorTransports] = credential.credDescriptor.transports.joinToString(separator = ",") { it }
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

    override suspend fun getCredentialBy(userHandle: Uuid, credentialId: ByteArray): PasskeyCredential? {
        return mapExceptions {
            dbQuery {
                PasskeyCredentialTable
                    .innerJoin(
                        AuthenticationTable,
                        onColumn = { authId },
                        otherColumn = { uuid },
                        additionalConstraint = { AuthenticationTable.uuid eq userHandle.toJavaUuid() }
                    )
                    .selectAll()
                    .map { PasskeyCredentialEntity.wrapRowR2dbc(it).toDomain() }
                    .singleOrNull()
            }
        }
    }

    override suspend fun getCredentialBy(authId: Uuid): List<PasskeyCredential> {
        return mapExceptions {
            dbQuery {
                AuthenticationTable
                    .innerJoin(
                        otherTable = PasskeyCredentialTable,
                        onColumn = { uuid },
                        otherColumn = { this.authId },
                    )
                    .select(PasskeyCredentialTable.columns)
                    .where { AuthenticationTable.id eq authId.toJavaUuid() }
                    .map { PasskeyCredentialEntity.wrapRowR2dbc(it).toDomain() }
                    .toList()
            }
        }
    }

    override suspend fun getUsernameByHandle(userHandle: Uuid): String? = mapExceptions {
        dbQuery {
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
            dbQuery {
                PasskeyCredentialTable
                    .innerJoin(
                        otherTable = AuthenticationTable,
                        onColumn = { authId },
                        otherColumn = { uuid },
                        additionalConstraint = { AuthenticationTable.uuid eq username.toJavaUuid() }
                    )
                    .selectAll()
                    .map { PasskeyCredentialEntity.wrapRowR2dbc(it).toDomain() }
                    .toSet()
            }
        }
    }

    override suspend fun getCredentialsByCredentialId(credentialId: ByteArray): Set<PasskeyCredential> {
        return mapExceptions {
            dbQuery {
                PasskeyCredentialTable
                    .innerJoin(
                        otherTable = AuthenticationTable,
                        onColumn = { authId },
                        otherColumn = { uuid },
                    )
                    .selectAll()
                    .where { PasskeyCredentialTable.credDescriptorId eq credentialId }
                    .map { PasskeyCredentialEntity.wrapRowR2dbc(it).toDomain() }
                    .toSet()
            }
        }
    }

    override suspend fun getHandleByUsername(username: Uuid): Uuid? = mapExceptions {
        dbQuery {
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
            dbQuery {
                PasskeyCredentialTable.update(where = { PasskeyCredentialTable.id eq passkeyCredentialId.toJavaUuid() }) {
                    it[lastUsedAt] = Clock.System.now()
                }
            }
        }
    }

    override suspend fun deletePasskey(id: Uuid, authId: Uuid): Int {
        return mapExceptions {
            dbQuery {
                val existingPasskeys = AuthenticationTable
                    .innerJoin(
                        otherTable = PasskeyCredentialTable,
                        onColumn = { uuid },
                        otherColumn = { this.authId }
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