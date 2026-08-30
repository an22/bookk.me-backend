package com.bookk.business.domain.api.employee.entity

import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class EmployeeInvitation(
    val id: Uuid,
    val businessId: Uuid,
    val invitedBy: Uuid,
    val email: String,
    val status: EmployeeInvitationStatus,
    val createdAt: Instant
) {
    companion object {
        fun stub(
            id: Uuid = Uuid.random(),
            businessId: Uuid = Uuid.random(),
            invitedBy: Uuid = Uuid.random(),
            email: String = "stub@employee.com",
            status: EmployeeInvitationStatus = EmployeeInvitationStatus.PENDING,
            createdAt: Instant = Instant.fromEpochMilliseconds(0)
        ) = EmployeeInvitation(
            id = id,
            businessId = businessId,
            invitedBy = invitedBy,
            email = email,
            status = status,
            createdAt = createdAt
        )
    }
}
