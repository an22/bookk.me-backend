package com.bookk.auth.data.datasource

import com.bookk.auth.data.map.toDomain
import com.bookk.auth.data.orm.entity.SigningKeyEntity
import com.bookk.auth.data.orm.table.SigningKeyTable
import com.bookk.auth.domain.api.token.entity.SigningKey
import com.bookk.auth.domain.api.token.entity.SigningKeyStatus
import com.bookk.auth.domain.datasource.SigningKeyDataSource
import com.bookk.core.data.DataSource
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

internal class SigningKeyDataSourceImpl : DataSource(), SigningKeyDataSource {

    override suspend fun getActiveKey(): SigningKey? = dbQuery {
        SigningKeyEntity.find { SigningKeyTable.status eq SigningKeyStatus.ACTIVE }
            .map { it.toDomain() }
            .singleOrNull()
    }

    override suspend fun getVerificationKeys(): List<SigningKey> = dbQuery {
        SigningKeyTable
            .selectAll()
            .where {
                SigningKeyTable.status.eq(SigningKeyStatus.ACTIVE)
                    .or(SigningKeyTable.status.eq(SigningKeyStatus.RETIRING))
            }
            .orderBy(SigningKeyTable.createdAt, SortOrder.DESC)
            .map { SigningKeyEntity.wrapRow(it).toDomain() }
            .toList()
    }

    override suspend fun insertKey(publicKeyPem: String, privateKeyPem: String): SigningKey = dbQuery {
        SigningKeyEntity.new {
            publicKey = publicKeyPem
            privateKey = privateKeyPem
            status = SigningKeyStatus.ACTIVE
            createdAt = Clock.System.now()
        }.toDomain()
    }

    override suspend fun updateStatus(id: Uuid, status: SigningKeyStatus, retiredAt: Instant?) = dbQuery<Unit> {
        SigningKeyEntity.findById(id.toJavaUuid())?.apply {
            this.status = status
            this.retiredAt = retiredAt
        }
    }

    override suspend fun deleteRetiredBefore(threshold: Instant) = dbQuery<Unit> {
        SigningKeyTable.deleteWhere {
            status.eq(SigningKeyStatus.RETIRING).and(retiredAt.less(threshold))
        }
    }
}
