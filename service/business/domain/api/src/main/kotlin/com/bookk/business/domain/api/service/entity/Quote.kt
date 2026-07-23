package com.bookk.business.domain.api.service.entity

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class Quote(
    val id: Uuid,
    val services: List<Service>,
    val token: String
) {
    companion object {
        fun stub(
            id: Uuid = Uuid.random(),
            services: List<Service> = emptyList(),
            token: String = "stub-quote-token"
        ) = Quote(id = id, services = services, token = token)
    }
}
