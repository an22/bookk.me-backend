package com.book.core.service.auth

class AppPrincipal(
    val userId: Long,
    val userName: String,
    val role: Int,
    val deviceId: Long
)