package com.bookk.server.business.client.api

import com.bookk.business.domain.api.business.entity.Business
import kotlinx.datetime.TimeZone
import kotlinx.serialization.Serializable
import library.schedule.Schedule
import kotlin.uuid.Uuid

@Serializable
data class BusinessDTO(
    val id: Uuid,
    val name: String,
    val address: String,
    val timeZone: TimeZone,
    val schedule: Schedule
) {
    companion object {
        fun from(business: Business) = BusinessDTO(
            id = business.id,
            name = business.name,
            address = business.address,
            timeZone = business.timeZone,
            schedule = business.schedule
        )
    }
}
