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
import org.jetbrains.exposed.v1.dao.with
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.update
import org.jetbrains.exposed.v1.jdbc.upsert
import kotlin.time.Clock
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

internal class EmployeeDataSourceImpl : DataSource(), EmployeeDataSource {

    override suspend fun createEmployee(employee: Employee): Employee = dbQuery {
        val id = EmployeeTable.insertAndGetId {
            it[businessId] = employee.businessId.toJavaUuid()
            it[name] = employee.name.trim()
            it[lastName] = employee.lastName.trim()
            it[phone] = employee.phone?.trim()
            it[email] = employee.email?.trim()
            it[userId] = employee.userId.toJavaUuid()
        }
        employee.services.forEach { service ->
            EmployeeCanProvideServiceTable.upsert {
                it[employeeId] = id
                it[serviceId] = service.id.toJavaUuid()
                it[businessId] = employee.businessId.toJavaUuid()
            }
        }
        employee.copy(id = id.value.toKotlinUuid())
    }

    override suspend fun getEmployees(businessId: Uuid): List<Employee> = dbQuery {
        EmployeeEntity.find {
            EmployeeTable.businessId eq businessId.toJavaUuid()
        }
            .toList()
            .with(EmployeeEntity::services)
            .map(EmployeeEntity::toDomain)
    }

    override suspend fun getEmployee(businessId: Uuid, id: Uuid): Employee? = dbQuery {
        EmployeeEntity.find {
            (EmployeeTable.businessId eq businessId.toJavaUuid()) and (EmployeeTable.id eq id.toJavaUuid())
        }
            .firstOrNull()
            ?.toDomain()
    }

    override suspend fun getEmployeeByUserId(businessId: Uuid, userId: Uuid): Employee? = dbQuery {
        EmployeeEntity.find {
            (EmployeeTable.businessId eq businessId.toJavaUuid()) and (EmployeeTable.userId eq userId.toJavaUuid())
        }
            .firstOrNull()
            ?.toDomain()
    }

    override suspend fun deleteEmployee(businessId: Uuid, id: Uuid): Boolean = dbQuery {
        EmployeeTable.deleteWhere {
            (EmployeeTable.businessId eq businessId.toJavaUuid()) and (EmployeeTable.id eq id.toJavaUuid())
        } != 0
    }

    override suspend fun updateIntegratedEmployees(
        userId: Uuid,
        name: String,
        lastName: String,
        phone: String,
        email: String
    ): Int = dbQuery {
        EmployeeTable.update(
            where = { EmployeeTable.userId eq userId.toJavaUuid() }
        ) {
            it[this.name] = name.trim()
            it[this.lastName] = lastName.trim()
            it[this.phone] = phone.trim()
            it[this.email] = email.trim()
            it[updatedAt] = Clock.System.now()
        }
    }

    override suspend fun assignService(businessId: Uuid, employeeId: Uuid, serviceId: Uuid) = dbQuery<Unit> {
        EmployeeCanProvideServiceTable.upsert {
            it[this.employeeId] = employeeId.toJavaUuid()
            it[this.serviceId] = serviceId.toJavaUuid()
            it[this.businessId] = businessId.toJavaUuid()
        }
    }

    override suspend fun unassignService(employeeId: Uuid, serviceId: Uuid): Boolean = dbQuery {
        EmployeeCanProvideServiceTable.deleteWhere {
            (EmployeeCanProvideServiceTable.employeeId eq employeeId.toJavaUuid()) and
                (EmployeeCanProvideServiceTable.serviceId eq serviceId.toJavaUuid())
        } != 0
    }

    override suspend fun getServiceIds(employeeId: Uuid): List<Uuid> = dbQuery {
        EmployeeCanProvideServiceTable
            .select(EmployeeCanProvideServiceTable.serviceId)
            .where { EmployeeCanProvideServiceTable.employeeId eq employeeId.toJavaUuid() }
            .map { it[EmployeeCanProvideServiceTable.serviceId].value.toKotlinUuid() }
    }

    override suspend fun getEmployeesByService(serviceId: Uuid): List<Employee> = dbQuery {
        val employeeIds = EmployeeCanProvideServiceTable
            .select(EmployeeCanProvideServiceTable.employeeId)
            .where { EmployeeCanProvideServiceTable.serviceId eq serviceId.toJavaUuid() }

        EmployeeEntity.find {
            EmployeeTable.id inSubQuery employeeIds
        }
            .toList()
            .with(EmployeeEntity::services)
            .map(EmployeeEntity::toDomain)
    }
}
