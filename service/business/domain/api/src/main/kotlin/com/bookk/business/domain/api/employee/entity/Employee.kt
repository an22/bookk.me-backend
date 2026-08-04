package com.bookk.business.domain.api.employee.entity

import com.bookk.business.domain.api.service.entity.Service
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class Employee(
    val id: Uuid,
    val businessId: Uuid,
    val name: String,
    val lastName: String,
    val phone: String?,
    val email: String?,
    val userId: Uuid,
    val services: List<Service>,
    val createdAt: Instant
) {
    companion object {
        fun stub(
            id: Uuid = Uuid.random(),
            businessId: Uuid = Uuid.random(),
            name: String = "stub-name",
            lastName: String = "stub-lastname",
            phone: String? = "+10000000000",
            email: String? = "stub@employee.com",
            userId: Uuid = Uuid.random(),
            services: List<Service> = emptyList(),
            createdAt: Instant = Instant.fromEpochMilliseconds(0)
        ) = Employee(
            id = id,
            businessId = businessId,
            name = name,
            lastName = lastName,
            phone = phone,
            email = email,
            userId = userId,
            services = services,
            createdAt = createdAt
        )
    }
}
