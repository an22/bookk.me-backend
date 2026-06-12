package com.bookk.appointments.data.orm.entity

import com.bookk.appointments.data.orm.table.AppointmentServicesTable
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

internal class AppointmentServiceEntity(id: EntityID<UUID>) : UUIDEntity(id) {

    var serviceId by AppointmentServicesTable.serviceId
    var serviceName by AppointmentServicesTable.serviceName
    var serviceGroupId by AppointmentServicesTable.serviceGroupId
    var priceCurrency by AppointmentServicesTable.priceCurrency
    var priceUnscaled by AppointmentServicesTable.priceUnscaled
    var priceScale by AppointmentServicesTable.priceScale
    var durationMinutes by AppointmentServicesTable.durationMinutes

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

    companion object : DecoratorUUIDEntityClass<AppointmentServiceEntity>(AppointmentServicesTable) {
        fun new(service: ServiceSnapshot): AppointmentServiceEntity = new {
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