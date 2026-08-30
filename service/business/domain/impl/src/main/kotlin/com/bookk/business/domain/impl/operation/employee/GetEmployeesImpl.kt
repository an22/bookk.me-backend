package com.bookk.business.domain.impl.operation.employee

import com.bookk.business.domain.api.employee.entity.Employee
import com.bookk.business.domain.api.employee.operation.GetEmployees
import com.bookk.business.domain.datasource.BusinessDataSource
import com.bookk.business.domain.datasource.EmployeeDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import library.permissions.ObjectPermission
import library.permissions.assert
import kotlin.uuid.Uuid

internal class GetEmployeesImpl(
    private val employeeDataSource: EmployeeDataSource,
    private val businessDataSource: BusinessDataSource,
    private val transactionManager: TransactionManager
) : GetEmployees {
    override suspend fun invoke(userId: Uuid, businessId: Uuid): Result<List<Employee>> = transactionManager.transaction {
        businessDataSource.getPermission(userId, businessId).assert(ObjectPermission.OWNER)
        employeeDataSource.getEmployees(businessId)
    }
}
