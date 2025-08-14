package com.book.business.domain.api.entity

import kotlinx.serialization.Serializable

@Serializable
class UserBusinesses(
    val dashboardId: Long,
    val businesses: List<Business>
)