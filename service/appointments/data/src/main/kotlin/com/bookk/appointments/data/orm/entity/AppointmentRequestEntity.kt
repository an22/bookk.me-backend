package com.bookk.appointments.data.orm.entity

import com.bookk.appointments.data.orm.table.AppointmentRequestServicesTable
import com.bookk.appointments.data.orm.table.AppointmentRequestTable
import com.bookk.appointments.data.orm.table.BusinessHasAppointments
import com.bookk.appointments.domain.api.entity.AppointmentRequest
import com.bookk.appointments.domain.api.entity.AppointmentRequestStatus
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
    val services by AppointmentRequestServiceEntity referrersOn AppointmentRequestServicesTable.requestId
    var dateStart by AppointmentRequestTable.dateStart
    var dateEnd by AppointmentRequestTable.dateEnd
    var note by AppointmentRequestTable.note
    var status by AppointmentRequestTable.status
    var declineReason by AppointmentRequestTable.declineReason

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
            declineReason = declineReason
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
            dateStart = request.date
            dateEnd = request.dateEnd
            note = request.note
            status = AppointmentRequestStatus.PENDING
            declineReason = request.declineReason
        }

        fun findByIdAndUpdate(request: AppointmentRequest) = findByIdAndUpdate(request.id.toJavaUuid()) {
            it.userId = request.userId.toJavaUuid()
            it.businessId = EntityID(request.businessId.toJavaUuid(), table = BusinessHasAppointments)
            it.clientId = request.client.id.toJavaUuid()
            it.clientName = request.client.fullName
            it.clientPhone = request.client.phone
            it.clientEmail = request.client.email
            it.dateStart = request.date
            it.dateEnd = request.dateEnd
            it.note = request.note
            it.status = request.status
            it.declineReason = request.declineReason
        }
    }
}