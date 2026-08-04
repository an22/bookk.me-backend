package com.bookk.business.data.orm.entity

import com.bookk.business.data.orm.table.EmployeeCanProvideServiceTable
import com.bookk.business.data.orm.table.EmployeeTable
import com.bookk.business.domain.api.employee.entity.Employee
import com.bookk.core.data.DecoratorUuidEntityClass
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UuidEntity
import kotlin.uuid.Uuid

internal class EmployeeEntity(id: EntityID<Uuid>) : UuidEntity(id) {

    var businessId by EmployeeTable.businessId
    var name by EmployeeTable.name
    var lastName by EmployeeTable.lastName
    var phone by EmployeeTable.phone
    var email by EmployeeTable.email
    var userId by EmployeeTable.userId
    var createdAt by EmployeeTable.createdAt
    var updatedAt by EmployeeTable.updatedAt

    val services by ServiceEntity via EmployeeCanProvideServiceTable

    companion object : DecoratorUuidEntityClass<EmployeeEntity>(EmployeeTable)

    fun toDomain(): Employee {
        return Employee(
            id = id.value,
            businessId = businessId.value,
            name = name,
            lastName = lastName,
            phone = phone,
            email = email,
            userId = userId,
            services = services.map(ServiceEntity::toDomain),
            createdAt = createdAt
        )
    }
}
