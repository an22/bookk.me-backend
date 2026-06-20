package com.bookk.business.domain.api.business.entity

import kotlinx.datetime.TimeZone
import kotlinx.serialization.Serializable

@Serializable
data class BusinessCreateRequest(
    val name: String,
    val currencyCode: String,
    val timeZone: TimeZone
)
