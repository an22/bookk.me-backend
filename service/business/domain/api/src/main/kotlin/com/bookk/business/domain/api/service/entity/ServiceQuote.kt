package com.bookk.business.domain.api.service.entity

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class ServiceQuote(
    val id: Uuid,
    val services: List<Service>,
    val token: String
) {
    companion object {
        fun stub(
            id: Uuid = Uuid.random(),
            services: List<Service> = emptyList(),
            token: String = "stub-quote-token"
        ) = ServiceQuote(id = id, services = services, token = token)
    }
}
