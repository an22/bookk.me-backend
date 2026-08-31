package com.bookk.business.domain.api.appointment.entity

import com.bookk.business.domain.api.client.entity.ClientRemote
import com.bookk.business.domain.api.employee.entity.Employee
import com.bookk.business.domain.api.service.entity.Service
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class AppointmentBookingContext(
    val employee: Employee,
    val client: ClientRemote,
    val services: List<Service>
) {
    companion object {
        fun stub(
            employee: Employee = Employee.stub(),
            client: ClientRemote = ClientRemote(
                id = Uuid.random(),
                name = "Client",
                lastName = "Name",
                phone = "123456789",
                email = "client@example.com",
                userId = null
            ),
            services: List<Service> = listOf(Service.stub(businessId = employee.businessId))
        ) = AppointmentBookingContext(employee = employee, client = client, services = services)
    }
}
