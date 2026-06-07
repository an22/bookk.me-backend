package com.bookk.appointments.domain.api.entity

import com.bookk.library.serializer.MoneySerializer
import kotlinx.serialization.Serializable
import org.joda.money.Money
import kotlin.time.Duration
import kotlin.uuid.Uuid

@Serializable
data class ServiceSnapshot(
    val id: Uuid,
    val name: String,
    val groupId: Uuid,
    @Serializable(with = MoneySerializer::class)
    val price: Money,
    val duration: Duration
) {
    companion object {
        fun stub() = ServiceSnapshot(
            id = Uuid.random(),
            name = "Service Name",
            groupId = Uuid.random(),
            price = Money.parse("USD 100"),
            duration = Duration.parse("30m")
        )
    }
}