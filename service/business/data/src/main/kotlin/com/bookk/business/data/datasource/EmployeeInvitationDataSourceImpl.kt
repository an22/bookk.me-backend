package com.bookk.business.data.datasource

import com.bookk.business.data.orm.entity.EmployeeInvitationEntity
import com.bookk.business.data.orm.table.EmployeeInvitationTable
import com.bookk.business.domain.api.employee.entity.EmployeeInvitation
import com.bookk.business.domain.api.employee.entity.EmployeeInvitationStatus
import com.bookk.business.domain.datasource.EmployeeInvitationDataSource
import com.bookk.core.data.DataSource
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.time.Clock
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

internal class EmployeeInvitationDataSourceImpl : DataSource(), EmployeeInvitationDataSource {

    override suspend fun createInvitation(invitation: EmployeeInvitation): EmployeeInvitation = dbQuery {
        val id = EmployeeInvitationTable.insertAndGetId {
            it[businessId] = invitation.businessId.toJavaUuid()
            it[userId] = invitation.userId.toJavaUuid()
            it[invitedBy] = invitation.invitedBy.toJavaUuid()
            it[name] = invitation.name.trim()
            it[lastName] = invitation.lastName.trim()
            it[phone] = invitation.phone?.trim()
            it[email] = invitation.email?.trim()
            it[status] = EmployeeInvitationStatus.PENDING
        }
        invitation.copy(id = id.value.toKotlinUuid(), status = EmployeeInvitationStatus.PENDING)
    }

    override suspend fun getInvitation(businessId: Uuid, id: Uuid): EmployeeInvitation? = dbQuery {
        EmployeeInvitationEntity.find {
            (EmployeeInvitationTable.businessId eq businessId.toJavaUuid()) and
                (EmployeeInvitationTable.id eq id.toJavaUuid())
        }
            .firstOrNull()
            ?.toDomain()
    }

    override suspend fun approveInvitation(id: Uuid): Boolean = dbQuery {
        EmployeeInvitationTable.update(
            where = {
                (EmployeeInvitationTable.id eq id.toJavaUuid()) and
                    (EmployeeInvitationTable.status eq EmployeeInvitationStatus.PENDING)
            }
        ) {
            it[status] = EmployeeInvitationStatus.APPROVED
            it[updatedAt] = Clock.System.now()
        } != 0
    }
}
