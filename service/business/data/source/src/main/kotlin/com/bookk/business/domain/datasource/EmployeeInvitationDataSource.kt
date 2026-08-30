package com.bookk.business.domain.datasource

import com.bookk.business.domain.api.employee.entity.EmployeeInvitation
import kotlin.time.Instant
import kotlin.uuid.Uuid

interface EmployeeInvitationDataSource {
    suspend fun createInvitation(invitation: EmployeeInvitation): EmployeeInvitation
    suspend fun getInvitation(businessId: Uuid, id: Uuid): EmployeeInvitation?
    suspend fun getPendingInvitations(businessId: Uuid, email: String): List<EmployeeInvitation>
    suspend fun approveInvitation(id: Uuid): Boolean
    suspend fun rejectInvitation(id: Uuid): Boolean
    suspend fun revokeInvitation(id: Uuid): Boolean
    suspend fun expireOldInvitations(before: Instant)
}
