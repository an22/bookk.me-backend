package com.bookk.business.data.datasource

import com.bookk.business.data.orm.entity.EmployeeEntity
import com.bookk.business.data.orm.table.EmployeeCanProvideServiceTable
import com.bookk.business.data.orm.table.EmployeeTable
import com.bookk.business.domain.api.employee.entity.Employee
import com.bookk.business.domain.datasource.EmployeeDataSource
import com.bookk.core.data.DataSource
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inSubQuery
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.dao.with
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.update
import org.jetbrains.exposed.v1.jdbc.upsert
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal class EmployeeDataSourceImpl : DataSource(), EmployeeDataSource {

    override suspend fun createEmployee(employee: Employee): Employee = dbQuery {
        val id = EmployeeTable.insertAndGetId {
            it[businessId] = employee.businessId
            it[name] = employee.name.trim()
            it[lastName] = employee.lastName.trim()
            it[phone] = employee.phone?.trim()
            it[email] = employee.email?.trim()
            it[userId] = employee.userId
        }
        employee.services.forEach { service ->
            EmployeeCanProvideServiceTable.upsert {
                it[employeeId] = id
                it[serviceId] = service.id
                it[businessId] = employee.businessId
            }
        }
        employee.copy(id = id.value)
    }

    override suspend fun getEmployees(businessId: Uuid): List<Employee> = dbQuery {
        EmployeeEntity.find {
            EmployeeTable.businessId eq businessId
        }
            .toList()
            .with(EmployeeEntity::services)
            .map(EmployeeEntity::toDomain)
    }

    override suspend fun getEmployee(businessId: Uuid, id: Uuid): Employee? = dbQuery {
        EmployeeEntity.find {
            (EmployeeTable.businessId eq businessId) and (EmployeeTable.id eq id)
        }
            .firstOrNull()
            ?.toDomain()
    }

    override suspend fun getEmployeeByUserId(businessId: Uuid, userId: Uuid): Employee? = dbQuery {
        EmployeeEntity.find {
            (EmployeeTable.businessId eq businessId) and (EmployeeTable.userId eq userId)
        }
            .firstOrNull()
            ?.toDomain()
    }

    override suspend fun deleteEmployee(businessId: Uuid, id: Uuid): Boolean = dbQuery {
        EmployeeTable.deleteWhere {
            (EmployeeTable.businessId eq businessId) and (EmployeeTable.id eq id)
        } != 0
    }

    override suspend fun updateIntegratedEmployees(
        userId: Uuid,
        name: String,
        lastName: String,
        email: String,
        phone: String?,
        updatedAt: Instant
    ): Int = dbQuery {
        EmployeeTable.update(
            where = {
                (EmployeeTable.userId eq userId) and
                    (EmployeeTable.sourceUpdatedAt.isNull() or (EmployeeTable.sourceUpdatedAt less updatedAt))
            }
        ) {
            it[this.name] = name.trim()
            it[this.lastName] = lastName.trim()
            it[this.email] = email.trim()
            phone?.let { value -> it[this.phone] = value.trim() }
            it[EmployeeTable.sourceUpdatedAt] = updatedAt
            it[EmployeeTable.updatedAt] = Clock.System.now()
        }
    }

    override suspend fun assignService(businessId: Uuid, employeeId: Uuid, serviceId: Uuid) = dbQuery<Unit> {
        EmployeeCanProvideServiceTable.upsert {
            it[this.employeeId] = employeeId
            it[this.serviceId] = serviceId
            it[this.businessId] = businessId
        }
    }

    override suspend fun unassignService(employeeId: Uuid, serviceId: Uuid): Boolean = dbQuery {
        EmployeeCanProvideServiceTable.deleteWhere {
            (EmployeeCanProvideServiceTable.employeeId eq employeeId) and
                (EmployeeCanProvideServiceTable.serviceId eq serviceId)
        } != 0
    }

    override suspend fun getServiceIds(employeeId: Uuid): List<Uuid> = dbQuery {
        EmployeeCanProvideServiceTable
            .select(EmployeeCanProvideServiceTable.serviceId)
            .where { EmployeeCanProvideServiceTable.employeeId eq employeeId }
            .map { it[EmployeeCanProvideServiceTable.serviceId].value }
    }

    override suspend fun getEmployeesByService(serviceId: Uuid): List<Employee> = dbQuery {
        val employeeIds = EmployeeCanProvideServiceTable
            .select(EmployeeCanProvideServiceTable.employeeId)
            .where { EmployeeCanProvideServiceTable.serviceId eq serviceId }

        EmployeeEntity.find {
            EmployeeTable.id inSubQuery employeeIds
        }
            .toList()
            .with(EmployeeEntity::services)
            .map(EmployeeEntity::toDomain)
    }
}
