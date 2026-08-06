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

internal class EmployeeInvitationDataSourceImpl : DataSource(), EmployeeInvitationDataSource {

    override suspend fun createInvitation(invitation: EmployeeInvitation): EmployeeInvitation = dbQuery {
        val id = EmployeeInvitationTable.insertAndGetId {
            it[businessId] = invitation.businessId
            it[invitedBy] = invitation.invitedBy
            it[email] = invitation.email.trim()
            it[status] = EmployeeInvitationStatus.PENDING
        }
        invitation.copy(id = id.value, status = EmployeeInvitationStatus.PENDING)
    }

    override suspend fun getInvitation(businessId: Uuid, id: Uuid): EmployeeInvitation? = dbQuery {
        EmployeeInvitationEntity.find {
            (EmployeeInvitationTable.businessId eq businessId) and
                (EmployeeInvitationTable.id eq id)
        }
            .firstOrNull()
            ?.toDomain()
    }

    override suspend fun getPendingInvitations(businessId: Uuid, email: String): List<EmployeeInvitation> = dbQuery {
        EmployeeInvitationEntity.find {
            (EmployeeInvitationTable.businessId eq businessId) and
                (EmployeeInvitationTable.email eq email) and
                (EmployeeInvitationTable.status eq EmployeeInvitationStatus.PENDING)
        }
            .toList()
            .map(EmployeeInvitationEntity::toDomain)
    }

    override suspend fun approveInvitation(id: Uuid): Boolean = dbQuery {
        EmployeeInvitationTable.update(
            where = {
                (EmployeeInvitationTable.id eq id) and
                    (EmployeeInvitationTable.status eq EmployeeInvitationStatus.PENDING)
            }
        ) {
            it[status] = EmployeeInvitationStatus.APPROVED
            it[updatedAt] = Clock.System.now()
        } != 0
    }
}
