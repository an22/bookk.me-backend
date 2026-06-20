package com.bookk.business.domain.api.business.entity

import kotlinx.datetime.TimeZone
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
class Business(
    val id: Uuid,
    val name: String,
    val description: String,
    val address: String,
    val timeZone: TimeZone,
    val location: Location?,
    val currencyCode: String,
    val socials: List<Social>
) {
    @Serializable
    class Location(
        val lat: Double,
        val lng: Double
    )

    @Serializable
    data class Social(
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

    companion object {
        const val MAX_NAME_LENGTH = 512
        const val MAX_DESCRIPTION_LENGTH = 1024
        const val MAX_CURRENCY_CODE = 3
        const val MAX_ADDRESS_LENGTH = 512
        const val MAX_SOCIAL_LENGTH = 256

        fun stub(
            id: Uuid = Uuid.random(),
            name: String = "Business name",
            description: String = "Business description",
            address: String = "Business address",
            timeZone: TimeZone = TimeZone.UTC,
            location: Location? = null,
            currencyCode: String = "USD",
            socials: List<Social> = emptyList()
        ) = Business(
            id = id,
            name = name,
            description = description,
            address = address,
            timeZone = timeZone,
            location = location,
            currencyCode = currencyCode,
            socials = socials
        )
    }
}