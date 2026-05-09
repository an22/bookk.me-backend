package com.bookk.business.domain.api.business.entity

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
class UserBusinesses(
    val dashboardId: Uuid?,
    val businesses: List<Business>
)