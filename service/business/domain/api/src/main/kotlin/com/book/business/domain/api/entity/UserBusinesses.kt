package com.book.business.domain.api.entity

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
class UserBusinesses(
    val dashboardId: Uuid?,
    val businesses: List<Business>
)