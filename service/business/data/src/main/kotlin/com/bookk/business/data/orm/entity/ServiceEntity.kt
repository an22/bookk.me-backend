package com.bookk.business.data.orm.entity

import com.bookk.business.data.orm.table.ServiceTable
import com.bookk.business.domain.api.service.entity.Service
import com.bookk.core.data.DecoratorUUIDEntityClass
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.joda.money.CurrencyUnit
import org.joda.money.Money
import java.math.BigDecimal
import java.math.BigInteger
import java.util.UUID
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.toKotlinUuid

internal class ServiceEntity(id: EntityID<UUID>) : UUIDEntity(id) {

    var businessId by ServiceTable.businessId
    var group by ServiceGroupEntity referencedOn ServiceTable.groupId
    var duration by ServiceTable.duration
    var name by ServiceTable.name
    var priceCurrency by ServiceTable.priceCurrency
    var priceUnscaled by ServiceTable.priceUnscaled
    var priceScale by ServiceTable.priceScale
    var available by ServiceTable.available
    var createdAt by ServiceTable.createdAt
    var updatedAt by ServiceTable.updatedAt

    companion object : DecoratorUUIDEntityClass<ServiceEntity>(ServiceTable)

    fun toDomain(): Service {
        return Service(
            id = id.value.toKotlinUuid(),
            businessId = businessId.value.toKotlinUuid(),
            group = group.toDomain(),
            duration = duration.minutes,
            name = name,
            price = Money.of(
                CurrencyUnit.of(priceCurrency),
                BigDecimal(BigInteger.valueOf(priceUnscaled), priceScale)
            ),
            isAvailable = available,
            createdAt = createdAt
        )
    }
}