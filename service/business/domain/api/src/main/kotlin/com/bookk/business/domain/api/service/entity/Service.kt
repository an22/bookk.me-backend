package com.bookk.business.domain.api.service.entity

import com.bookk.library.serializer.MoneySerializer
import kotlinx.serialization.Serializable
import org.joda.money.Money
import kotlin.time.Duration
import kotlin.uuid.Uuid

@Serializable
data class Service(
    val id: Uuid,
    val businessId: Uuid,
    val group: ServiceGroup,
    val name: String,
    val duration: Duration,
    @Serializable(with = MoneySerializer::class)
    val price: Money
)