package com.bookk.appointments.domain.api.entity

import com.bookk.library.serializer.MoneySerializer
import kotlinx.serialization.Serializable
import org.joda.money.Money
import kotlin.time.Duration
import kotlin.uuid.Uuid

@Serializable
class ServiceSnapshot(
    val id: Uuid,
    val name: String,
    val groupId: Uuid,
    @Serializable(with = MoneySerializer::class)
    val price: Money,
    val duration: Duration
)