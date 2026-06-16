package com.bookk.appointments.data.orm.entity

import com.bookk.appointments.data.orm.table.AppointmentRequestServicesTable
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

internal class AppointmentRequestServiceEntity(id: EntityID<UUID>) : UUIDEntity(id) {

    var requestId by AppointmentRequestServicesTable.requestId
    var serviceId by AppointmentRequestServicesTable.serviceId
    var serviceName by AppointmentRequestServicesTable.serviceName
    var serviceGroupId by AppointmentRequestServicesTable.serviceGroupId
    var priceCurrency by AppointmentRequestServicesTable.priceCurrency
    var priceUnscaled by AppointmentRequestServicesTable.priceUnscaled
    var priceScale by AppointmentRequestServicesTable.priceScale
    var durationMinutes by AppointmentRequestServicesTable.durationMinutes

    fun domain(): ServiceSnapshot {
        return ServiceSnapshot(
            id = serviceId.toKotlinUuid(),
            name = serviceName,
            groupId = serviceGroupId.toKotlinUuid(),
            price = Money.of(
                CurrencyUnit.of(priceCurrency),
                BigDecimal(BigInteger.valueOf(priceUnscaled), priceScale)
            ),
            duration = durationMinutes.minutes
        )
    }

    companion object : DecoratorUUIDEntityClass<AppointmentRequestServiceEntity>(AppointmentRequestServicesTable) {
        fun new(ownerId: EntityID<UUID>, service: ServiceSnapshot): AppointmentRequestServiceEntity = new {
            requestId = ownerId
            serviceId = service.id.toJavaUuid()
            serviceName = service.name
            serviceGroupId = service.groupId.toJavaUuid()
            priceCurrency = service.price.currencyUnit.code
            priceUnscaled = service.price.amount.unscaledValue().longValueExact()
            priceScale = service.price.amount.scale()
            durationMinutes = service.duration.inWholeMinutes
        }
    }
}