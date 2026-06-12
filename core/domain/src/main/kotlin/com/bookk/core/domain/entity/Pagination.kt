package com.bookk.core.domain.entity

import kotlinx.serialization.Serializable

@Serializable
class Pagination<T>(
    val data: List<T>,
    val total: Long,
    val page: Long,
    val pageSize: Int,
)