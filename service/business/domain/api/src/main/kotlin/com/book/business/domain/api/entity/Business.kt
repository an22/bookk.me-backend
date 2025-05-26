package com.book.business.domain.api.entity

import kotlinx.serialization.Serializable

@Serializable
class Business(
    val id: Long,
    val name: String
)