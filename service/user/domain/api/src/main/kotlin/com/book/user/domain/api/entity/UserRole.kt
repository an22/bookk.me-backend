package com.book.user.domain.api.entity

import kotlinx.serialization.Serializable

@Serializable
enum class UserRole(val id: Int) {
    BUSINESS_OWNER(1),
    CLIENT(2),
    EMPLOYEE(3)
}