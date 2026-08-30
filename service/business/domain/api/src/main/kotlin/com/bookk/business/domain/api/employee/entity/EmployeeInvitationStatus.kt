package com.bookk.business.domain.api.employee.entity

import kotlinx.serialization.Serializable

@Serializable
enum class EmployeeInvitationStatus {
    PENDING,
    APPROVED,
    REJECTED,
    EXPIRED,
    REVOKED
}
