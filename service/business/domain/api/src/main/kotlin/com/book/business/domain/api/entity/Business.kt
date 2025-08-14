package com.book.business.domain.api.entity

import kotlinx.serialization.Serializable

@Serializable
class Business(
    val id: Long,
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