package com.bookk.business.domain.api.employee.operation

import com.bookk.business.domain.api.business.entity.BusinessPermissions
import kotlin.uuid.Uuid

interface GetEmployeePermissions {
    suspend operator fun invoke(requestUserId: Uuid, businessId: Uuid, employeeId: Uuid): Result<BusinessPermissions>
}
