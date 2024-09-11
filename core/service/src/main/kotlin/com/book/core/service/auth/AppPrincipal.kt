package com.book.core.service.auth

import io.ktor.server.auth.*

class AppPrincipal(
    val userId: Long,
    val userName: String,
    val role: Int,
    val deviceId: Long
) : Principal