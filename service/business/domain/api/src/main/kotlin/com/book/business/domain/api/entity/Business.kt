package com.book.business.domain.api.entity

import com.book.library.serializer.MoneySerializer
import kotlinx.serialization.Serializable
import org.joda.money.CurrencyUnit

@Serializable
class Business(
    val id: Long,
    val name: String,
    val description: String?,
    val location: Location?,
    @Serializable(with = MoneySerializer::class)
    val currency: CurrencyUnit?,
    val socials: List<Social>
) {
    @Serializable
    class Location(
        val lat: Double,
        val lng: Double
    )

    @Serializable
    class Social(
        val kind: SocialKind,
        val value: String?
    )

    enum class SocialKind {
        PHONE,
        INSTAGRAM,
        TELEGRAM,
        VIBER,
        WHATSAPP
    }
}