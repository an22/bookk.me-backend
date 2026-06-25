package com.bookk.core.domain.entity

import kotlinx.serialization.Serializable

@Serializable
class PaginationMetadata(
    val total: Long,
    val page: Long,
    val pageSize: Int,
)