package com.book.core.data.eventstreaming

import kotlinx.coroutines.CoroutineScope

interface EventHandler {
    fun start(scope: CoroutineScope)
}