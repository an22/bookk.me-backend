package com.bookk.business.data.orm.entity

import com.bookk.business.data.orm.table.EmployeeInvitationTable
import com.bookk.business.domain.api.employee.entity.EmployeeInvitation
import com.bookk.core.data.DecoratorUuidEntityClass
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UuidEntity
import kotlin.uuid.Uuid

internal class EmployeeInvitationEntity(id: EntityID<Uuid>) : UuidEntity(id) {

    var businessId by EmployeeInvitationTable.businessId
    var userId by EmployeeInvitationTable.userId
    var invitedBy by EmployeeInvitationTable.invitedBy
    var name by EmployeeInvitationTable.name
    var lastName by EmployeeInvitationTable.lastName
    var phone by EmployeeInvitationTable.phone
    var email by EmployeeInvitationTable.email
    var status by EmployeeInvitationTable.status
    var createdAt by EmployeeInvitationTable.createdAt
    var updatedAt by EmployeeInvitationTable.updatedAt

    companion object : DecoratorUuidEntityClass<EmployeeInvitationEntity>(EmployeeInvitationTable)

    fun toDomain(): EmployeeInvitation {
        return EmployeeInvitation(
            id = id.value,
            businessId = businessId.value,
            userId = userId,
            invitedBy = invitedBy,
            name = name,
            lastName = lastName,
            phone = phone,
            email = email,
            status = status,
            createdAt = createdAt
        )
    }
}
