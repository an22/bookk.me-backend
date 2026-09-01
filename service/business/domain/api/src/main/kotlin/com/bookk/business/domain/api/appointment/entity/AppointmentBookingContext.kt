package com.bookk.business.domain.api.appointment.entity

import com.bookk.business.domain.api.client.entity.ClientRemote
import com.bookk.business.domain.api.employee.entity.Employee
import com.bookk.business.domain.api.service.entity.Service
import kotlinx.serialization.Serializable

@Serializable
data class AppointmentBookingContext(
    val employee: Employee,
    val client: ClientRemote,
    val services: List<Service>
) {
    companion object {
        fun stub(
            employee: Employee = Employee.stub(),
            client: ClientRemote = ClientRemote.stub(),
            services: List<Service> = listOf(Service.stub(businessId = employee.businessId))
        ) = AppointmentBookingContext(employee = employee, client = client, services = services)
    }
}
