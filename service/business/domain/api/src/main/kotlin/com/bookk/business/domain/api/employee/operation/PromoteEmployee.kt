package com.bookk.business.domain.api.employee.operation

import com.bookk.business.domain.api.employee.entity.EmployeeRole
import kotlin.uuid.Uuid

interface PromoteEmployee {
    suspend operator fun invoke(requestUserId: Uuid, businessId: Uuid, employeeId: Uuid, role: EmployeeRole): Result<Unit>
}
