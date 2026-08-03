package com.bookk.business.domain.datasource

import com.bookk.business.domain.api.employee.entity.EmployeeInvitation
import kotlin.uuid.Uuid

interface EmployeeInvitationDataSource {
    suspend fun createInvitation(invitation: EmployeeInvitation): EmployeeInvitation
    suspend fun getInvitation(businessId: Uuid, id: Uuid): EmployeeInvitation?
    suspend fun approveInvitation(id: Uuid): Boolean
}
