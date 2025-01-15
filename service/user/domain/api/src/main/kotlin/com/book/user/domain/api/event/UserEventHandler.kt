package com.book.user.domain.api.event

import kotlinx.coroutines.CoroutineScope

interface UserEventHandler {
    fun start(scope: CoroutineScope)
}