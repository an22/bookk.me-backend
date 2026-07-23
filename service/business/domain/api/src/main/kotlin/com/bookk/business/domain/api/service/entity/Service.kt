package com.bookk.business.domain.api.service.entity

import com.bookk.library.serializer.MoneySerializer
import kotlinx.serialization.Serializable
import org.joda.money.Money
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class Service(
    val id: Uuid,
    val businessId: Uuid,
    val group: ServiceGroup,
    val name: String,
    val duration: Duration,
    @Serializable(with = MoneySerializer::class)
    val price: Money,
    val isAvailable: Boolean,
    val createdAt: Instant
) {
    companion object {
        fun stub(
            id: Uuid = Uuid.random(),
            businessId: Uuid = Uuid.random(),
            group: ServiceGroup = ServiceGroup.stub(businessId = businessId),
            name: String = "stub-service",
            duration: Duration = 30.minutes,
            price: Money = Money.parse("USD 50"),
            isAvailable: Boolean = true,
            createdAt: Instant = Instant.fromEpochMilliseconds(0)
        ) = Service(
            id = id,
            businessId = businessId,
            group = group,
            name = name,
            duration = duration,
            price = price,
            isAvailable = isAvailable,
            createdAt = createdAt
        )
    }
}