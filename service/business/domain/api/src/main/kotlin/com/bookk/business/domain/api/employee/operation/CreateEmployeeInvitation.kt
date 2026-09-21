package com.bookk.business.domain.api.employee.operation

import com.bookk.business.domain.api.employee.entity.EmployeeInvitation
import kotlin.uuid.Uuid

interface CreateEmployeeInvitation {
    suspend operator fun invoke(requestUserId: Uuid, businessId: Uuid): Result<EmployeeInvitation>
}
