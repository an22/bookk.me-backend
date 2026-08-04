package com.bookk.business.domain.api.employee.entity

import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class EmployeeInvitation(
    val id: Uuid,
    val businessId: Uuid,
    val userId: Uuid,
    val invitedBy: Uuid,
    val name: String,
    val lastName: String,
    val phone: String?,
    val email: String?,
    val status: EmployeeInvitationStatus,
    val createdAt: Instant
) {
    companion object {
        fun stub(
            id: Uuid = Uuid.random(),
            businessId: Uuid = Uuid.random(),
            userId: Uuid = Uuid.random(),
            invitedBy: Uuid = Uuid.random(),
            name: String = "stub-name",
            lastName: String = "stub-lastname",
            phone: String? = "+10000000000",
            email: String? = "stub@employee.com",
            status: EmployeeInvitationStatus = EmployeeInvitationStatus.PENDING,
            createdAt: Instant = Instant.fromEpochMilliseconds(0)
        ) = EmployeeInvitation(
            id = id,
            businessId = businessId,
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
