package com.bookk.appointments.data.orm.entity

import com.bookk.appointments.data.orm.table.AppointmentRequestTable
import com.bookk.appointments.data.orm.table.BusinessHasAppointments
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

internal class AppointmentRequestEntity(id: EntityID<UUID>) : UUIDEntity(id) {

    var userId by AppointmentRequestTable.userId
    var businessId by AppointmentRequestTable.businessId
    var clientId by AppointmentRequestTable.clientId
    var clientName by AppointmentRequestTable.clientName
    var clientPhone by AppointmentRequestTable.clientPhone
    var clientEmail by AppointmentRequestTable.clientEmail
    var serviceId by AppointmentRequestTable.serviceId
    var serviceName by AppointmentRequestTable.serviceName
    var serviceGroupId by AppointmentRequestTable.serviceGroupId
    var priceCurrency by AppointmentRequestTable.priceCurrency
    var priceUnscaled by AppointmentRequestTable.priceUnscaled
    var priceScale by AppointmentRequestTable.priceScale
    var durationMinutes by AppointmentRequestTable.durationMinutes
    var dateStart by AppointmentRequestTable.dateStart
    var dateEnd by AppointmentRequestTable.dateEnd
    var note by AppointmentRequestTable.note

    fun domain(): AppointmentRequest {
        return AppointmentRequest(
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

    companion object : DecoratorUUIDEntityClass<AppointmentRequestEntity>(AppointmentRequestTable) {

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

        fun findByIdAndUpdate(request: AppointmentRequest) = findByIdAndUpdate(request.id.toJavaUuid()) {
            it.userId = request.userId.toJavaUuid()
            it.businessId = EntityID(request.businessId.toJavaUuid(), table = BusinessHasAppointments)
            it.clientId = request.client.id.toJavaUuid()
            it.clientName = request.client.fullName
            it.clientPhone = request.client.phone
            it.clientEmail = request.client.email
            it.serviceId = request.service.id.toJavaUuid()
            it.serviceName = request.service.name
            it.serviceGroupId = request.service.groupId.toJavaUuid()
            it.priceCurrency = request.service.price.currencyUnit.code
            it.priceUnscaled = request.service.price.amount.unscaledValue().longValueExact()
            it.priceScale = request.service.price.scale
            it.durationMinutes = request.service.duration.inWholeMinutes
            it.dateStart = request.date
            it.dateEnd = request.date + request.service.duration
            it.note = request.note
        }
    }
}