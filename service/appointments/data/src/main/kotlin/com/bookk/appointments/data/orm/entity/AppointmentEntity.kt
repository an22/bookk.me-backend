package com.bookk.appointments.data.orm.entity

import com.bookk.appointments.data.orm.table.AppointmentBusinessTable
import com.bookk.appointments.data.orm.table.AppointmentServicesTable
import com.bookk.appointments.data.orm.table.AppointmentTable
import com.bookk.appointments.domain.api.entity.Appointment
import com.bookk.appointments.domain.api.entity.AppointmentRequest
import com.bookk.appointments.domain.api.entity.AppointmentStatus
import com.bookk.appointments.domain.api.entity.ClientSnapshot
import com.bookk.appointments.domain.api.entity.ServiceSnapshot
import com.bookk.core.data.DecoratorUUIDEntityClass
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.joda.money.CurrencyUnit
import org.joda.money.Money
import java.math.BigDecimal
import java.math.BigInteger
import java.util.UUID
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

internal class AppointmentEntity(id: EntityID<UUID>) : UUIDEntity(id) {

    var userId by AppointmentTable.userId
    var businessId by AppointmentTable.businessId
    var clientId by AppointmentTable.clientId
    var clientName by AppointmentTable.clientName
    var clientPhone by AppointmentTable.clientPhone
    var clientEmail by AppointmentTable.clientEmail
    val services by AppointmentServiceEntity referrersOn AppointmentServicesTable.appointmentId
    var dateStart by AppointmentTable.dateStart
    var dateEnd by AppointmentTable.dateEnd
    var note by AppointmentTable.note
    var status by AppointmentTable.status
    var cancellationReason by AppointmentTable.cancellationReason

    fun domain(): Appointment {
        return Appointment(
            id = id.value.toKotlinUuid(),
            userId = userId.toKotlinUuid(),
            businessId = businessId.value.toKotlinUuid(),
            client = ClientSnapshot(
                id = clientId.toKotlinUuid(),
                fullName = clientName,
                phone = clientPhone.orEmpty(),
                email = clientEmail.orEmpty()
            ),
            services = services.map {
                ServiceSnapshot(
                    id = it.serviceId.toKotlinUuid(),
                    name = it.serviceName,
                    groupId = it.serviceGroupId.toKotlinUuid(),
                    price = Money.of(
                        CurrencyUnit.of(it.priceCurrency),
                        BigDecimal(BigInteger.valueOf(it.priceUnscaled), it.priceScale)
                    ),
                    duration = it.durationMinutes.minutes
                )
            },
            date = dateStart,
            note = note,
            status = status,
            cancellationReason = cancellationReason
        )
    }

    companion object : DecoratorUUIDEntityClass<AppointmentEntity>(AppointmentTable) {

        fun new(request: AppointmentRequest) = new {
            userId = request.userId.toJavaUuid()
            businessId = EntityID(request.businessId.toJavaUuid(), table = AppointmentBusinessTable)
            clientId = request.client.id.toJavaUuid()
            clientName = request.client.fullName
            clientPhone = request.client.phone
            clientEmail = request.client.email
            dateStart = request.date
            dateEnd = request.dateEnd
            note = request.note
            status = AppointmentStatus.SCHEDULED
            cancellationReason = ""
        }

        fun new(appointment: Appointment) = new {
            userId = appointment.userId.toJavaUuid()
            businessId = EntityID(appointment.businessId.toJavaUuid(), table = AppointmentBusinessTable)
            clientId = appointment.client.id.toJavaUuid()
            clientName = appointment.client.fullName
            clientPhone = appointment.client.phone
            clientEmail = appointment.client.email
            dateStart = appointment.date
            dateEnd = appointment.dateEnd
            note = appointment.note
            status = AppointmentStatus.SCHEDULED
            cancellationReason = ""
        }

        fun findByIdAndUpdate(appointment: Appointment) = findByIdAndUpdate(appointment.id.toJavaUuid()) {
            it.userId = appointment.userId.toJavaUuid()
            it.businessId = EntityID(appointment.businessId.toJavaUuid(), table = AppointmentBusinessTable)
            it.clientId = appointment.client.id.toJavaUuid()
            it.clientName = appointment.client.fullName
            it.clientPhone = appointment.client.phone
            it.clientEmail = appointment.client.email
            it.dateStart = appointment.date
            it.dateEnd = appointment.dateEnd
            it.note = appointment.note
            it.status = appointment.status
            it.cancellationReason = appointment.cancellationReason
        }
    }
}