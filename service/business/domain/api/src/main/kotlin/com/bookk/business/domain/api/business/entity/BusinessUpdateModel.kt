package com.bookk.business.domain.api.business.entity

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class BusinessUpdateModel(
    val id: Uuid,
    val name: String?,
    val description: String?,
    val address: String?,
    val location: Business.Location?,
    val currencyCode: String?,
    val socials: List<Business.Social>?
)