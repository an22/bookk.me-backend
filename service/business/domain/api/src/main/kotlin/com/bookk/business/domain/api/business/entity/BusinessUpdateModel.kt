package com.bookk.business.domain.api.business.entity

import kotlinx.datetime.TimeZone
import kotlinx.serialization.Serializable
import library.schedule.Schedule
import kotlin.uuid.Uuid

@Serializable
data class BusinessUpdateModel(
    val id: Uuid,
    val name: String?,
    val description: String?,
    val address: String?,
    val location: Business.Location?,
    val currencyCode: String?,
    val timeZone: TimeZone?,
    val socials: List<Business.Social>?,
    val schedule: Schedule?
)