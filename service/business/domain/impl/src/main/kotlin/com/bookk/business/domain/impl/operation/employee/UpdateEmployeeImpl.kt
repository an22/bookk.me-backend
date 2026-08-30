package com.bookk.business.domain.impl.operation.employee

import com.bookk.business.domain.api.employee.entity.Employee
import com.bookk.business.domain.api.employee.operation.UpdateEmployee
import com.bookk.business.domain.datasource.BusinessDataSource
import com.bookk.business.domain.datasource.EmployeeDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import library.permissions.ObjectPermission
import library.permissions.assert
import library.validation.EmailValidator
import library.validation.NameValidator
import library.validation.PhoneValidator
import kotlin.uuid.Uuid

internal class UpdateEmployeeImpl(
    private val employeeDataSource: EmployeeDataSource,
    private val businessDataSource: BusinessDataSource,
    private val transactionManager: TransactionManager
) : UpdateEmployee {
    override suspend fun invoke(requestUserId: Uuid, employee: Employee): Result<Employee> =
        transactionManager.transaction {
            businessDataSource.getPermission(requestUserId, employee.businessId).assert(ObjectPermission.EDIT)
            if (!NameValidator.isValid(employee.name) || !NameValidator.isValid(employee.lastName)) {
                throw UpdateEmployee.Error.ValidationError()
            }
            if (employee.phone?.let { !PhoneValidator.isValid(it) } == true) {
                throw UpdateEmployee.Error.ValidationError()
            }
            if (employee.email?.let { !EmailValidator.isValid(it) } == true) {
                throw UpdateEmployee.Error.ValidationError()
            }
            if (employee.schedule.days.values.any { it.isActive && it.workingTime.isEmpty() }) {
                throw UpdateEmployee.Error.ActiveDayWithoutWorkHours()
            }
            if (employee.schedule.dayOffs.any { it.start > it.end }) {
                throw UpdateEmployee.Error.InvalidDayOffRange()
            }
            employeeDataSource.updateEmployee(employee)
        }
}
