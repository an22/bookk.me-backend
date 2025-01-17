package com.book.auth.data.datasource

import com.book.auth.data.orm.entity.AuthenticationEntity
import com.book.auth.data.orm.entity.PasskeyCredentialEntity
import com.book.auth.domain.api.entity.PasskeyCredential
import com.book.auth.domain.datasource.PassKeyDataSource
import com.book.core.data.DataSource
import com.book.core.data.cache.CacheClient
import com.book.core.data.cache.get
import com.book.core.data.cache.set
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.time.Duration.Companion.minutes

internal class PassKeyDataSourceImpl(
    private val cacheClient: CacheClient<String>
) : DataSource(), PassKeyDataSource {

    override suspend fun saveChallengeToCache(base64Handle: String, options: String) = mapExceptions {
        cacheClient.withTransaction {
            set(base64Handle, options)
            setExpiration(base64Handle, 5.minutes)
        }
    }

    override suspend fun getCachedChallenge(base64Handle: String): String? {
        return mapExceptions { cacheClient.get(base64Handle) }
    }

    override suspend fun deleteCredentialOptions(base64Handle: String) {
        mapExceptions {
            cacheClient.delete(base64Handle)
        }
    }

    override suspend fun createPasskeyCredential(credential: PasskeyCredential) {
        mapExceptions {
            transaction {
                PasskeyCredentialEntity.new {
                    authId = AuthenticationEntity[credential.authId]
                    userHandle = credential.userHandle
                    credDescriptorId = credential.credDescriptorId
                    credDescriptorType = credential.credDescriptorType
                    credDescriptorTransports = credential.credDescriptorTransports
                    publicKey = credential.publicKey
                    signatureCount = credential.signatureCount
                    isDiscoverable = credential.isDiscoverable
                    isBackupEligible = credential.isBackupEligible
                    isBackedUp = credential.isBackedUp
                    attestationObject = credential.attestationObject
                    clientData = credential.clientData
                    updatedAt = Clock.System.now()
                }
            }
        }
    }
}