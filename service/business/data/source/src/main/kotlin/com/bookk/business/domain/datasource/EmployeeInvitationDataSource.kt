package com.bookk.business.domain.datasource

import com.bookk.business.domain.api.employee.entity.EmployeeInvitation
import kotlin.time.Instant
import kotlin.uuid.Uuid

interface EmployeeInvitationDataSource {
    suspend fun createInvitation(invitation: EmployeeInvitation): EmployeeInvitation
    suspend fun getInvitation(businessId: Uuid, id: Uuid): EmployeeInvitation?
    suspend fun getInvitationByCode(code: String): EmployeeInvitation?
    suspend fun getInvitationsByInviter(businessId: Uuid, invitedBy: Uuid): List<EmployeeInvitation>
    suspend fun redeemInvitation(id: Uuid): Boolean
    suspend fun revokeInvitation(id: Uuid): Boolean
    suspend fun expireOldInvitations(before: Instant)
}
