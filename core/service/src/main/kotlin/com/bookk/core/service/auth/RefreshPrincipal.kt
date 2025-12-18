package com.bookk.core.service.auth

import kotlin.uuid.Uuid

class RefreshPrincipal(
    val tokenId: Uuid,
    val deviceId: Uuid
)