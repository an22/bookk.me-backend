package com.book.business.domain.api.entity

import kotlinx.serialization.Serializable

@Serializable
class BusinessUpdateModel(
    val id: Long,
    val name: String?,
    val description: String?,
    val address: String?,
    val location: Business.Location?,
    val currencyCode: String?,
    val socials: List<Business.Social>?
)