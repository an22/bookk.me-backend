package com.bookk.core.service.auth

import kotlin.uuid.Uuid

class AppPrincipal(
    val authId: Uuid,
    val userId: Uuid,
    val deviceId: Uuid
)