package com.bookk.business.domain.api.entity

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
sealed interface Client {
    val id: Uuid
    val name: String
    val lastName: String
    val phone: String

    @Serializable
    data class Detached(
        override val id: Uuid,
        override val name: String,
        override val lastName: String,
        override val phone: String
    ) : Client

    @Serializable
    data class Integrated(
        override val id: Uuid,
        override val name: String,
        override val lastName: String,
        override val phone: String,
        val userId: Uuid
    ) : Client
}