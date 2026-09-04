package com.bookk.business.domain.api.business.entity

import kotlinx.serialization.Serializable

@Serializable
enum class BusinessResource {
    BUSINESS,
    EMPLOYEES,
    CLIENTS,
    SERVICES,
    APPOINTMENTS
}
