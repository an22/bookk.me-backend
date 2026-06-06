package com.bookk.appointments.data.orm.entity

import com.bookk.appointments.data.orm.table.AppointmentTable
import com.bookk.appointments.data.orm.table.BusinessHasAppointments
import com.bookk.appointments.domain.api.entity.Appointment
import com.bookk.appointments.domain.api.entity.AppointmentRequest
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
    var serviceId by AppointmentTable.serviceId
    var serviceName by AppointmentTable.serviceName
    var serviceGroupId by AppointmentTable.serviceGroupId
    var priceCurrency by AppointmentTable.priceCurrency
    var priceUnscaled by AppointmentTable.priceUnscaled
    var priceScale by AppointmentTable.priceScale
    var durationMinutes by AppointmentTable.durationMinutes
    var dateStart by AppointmentTable.dateStart
    var dateEnd by AppointmentTable.dateEnd
    var note by AppointmentTable.note

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
            service = ServiceSnapshot(
                id = serviceId.toKotlinUuid(),
                name = serviceName,
                groupId = serviceGroupId.toKotlinUuid(),
                price = Money.of(
                    CurrencyUnit.of(priceCurrency),
                    BigDecimal(BigInteger.valueOf(priceUnscaled), priceScale)
                ),
                duration = durationMinutes.minutes
            ),
            date = dateStart,
            note = note
        )
    }

    companion object : DecoratorUUIDEntityClass<AppointmentEntity>(AppointmentTable) {

        fun new(request: AppointmentRequest) = new {
            userId = request.userId.toJavaUuid()
            businessId = EntityID(request.businessId.toJavaUuid(), table = BusinessHasAppointments)
            clientId = request.client.id.toJavaUuid()
            clientName = request.client.fullName
            clientPhone = request.client.phone
            clientEmail = request.client.email
            serviceId = request.service.id.toJavaUuid()
            serviceName = request.service.name
            serviceGroupId = request.service.groupId.toJavaUuid()
            priceCurrency = request.service.price.currencyUnit.code
            priceUnscaled = request.service.price.amount.unscaledValue().longValueExact()
            priceScale = request.service.price.scale
            durationMinutes = request.service.duration.inWholeMinutes
            dateStart = request.date
            dateEnd = request.date + request.service.duration
            note = request.note
        }

        fun findByIdAndUpdate(appointment: Appointment) = findByIdAndUpdate(appointment.id.toJavaUuid()) {
            it.userId = appointment.userId.toJavaUuid()
            it.businessId = EntityID(appointment.businessId.toJavaUuid(), table = BusinessHasAppointments)
            it.clientId = appointment.client.id.toJavaUuid()
            it.clientName = appointment.client.fullName
            it.clientPhone = appointment.client.phone
            it.clientEmail = appointment.client.email
            it.serviceId = appointment.service.id.toJavaUuid()
            it.serviceName = appointment.service.name
            it.serviceGroupId = appointment.service.groupId.toJavaUuid()
            it.priceCurrency = appointment.service.price.currencyUnit.code
            it.priceUnscaled = appointment.service.price.amount.unscaledValue().longValueExact()
            it.priceScale = appointment.service.price.scale
            it.durationMinutes = appointment.service.duration.inWholeMinutes
            it.dateStart = appointment.date
            it.dateEnd = appointment.date + appointment.service.duration
            it.note = appointment.note
        }
    }
}