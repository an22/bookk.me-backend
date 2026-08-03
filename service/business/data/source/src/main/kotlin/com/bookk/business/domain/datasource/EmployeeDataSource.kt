package com.bookk.business.domain.datasource

import com.bookk.business.domain.api.employee.entity.Employee
import kotlin.uuid.Uuid

interface EmployeeDataSource {
    suspend fun createEmployee(employee: Employee): Employee
    suspend fun getEmployees(businessId: Uuid): List<Employee>
    suspend fun getEmployee(businessId: Uuid, id: Uuid): Employee?
    suspend fun deleteEmployee(businessId: Uuid, id: Uuid): Boolean
    suspend fun updateIntegratedEmployees(
        userId: Uuid,
        name: String,
        lastName: String,
        phone: String,
        email: String
    ): Int

    suspend fun assignService(businessId: Uuid, employeeId: Uuid, serviceId: Uuid)
    suspend fun unassignService(employeeId: Uuid, serviceId: Uuid): Boolean
    suspend fun getServiceIds(employeeId: Uuid): List<Uuid>
    suspend fun getEmployeesByService(serviceId: Uuid): List<Employee>
}
