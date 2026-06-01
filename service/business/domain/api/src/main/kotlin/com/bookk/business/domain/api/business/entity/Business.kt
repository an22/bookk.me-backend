package com.bookk.business.domain.api.business.entity

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
class Business(
    val id: Uuid,
    val name: String,
    val description: String,
    val address: String,
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
    }
}