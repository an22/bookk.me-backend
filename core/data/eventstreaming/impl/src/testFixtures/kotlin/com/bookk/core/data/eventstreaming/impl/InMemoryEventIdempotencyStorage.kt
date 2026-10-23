package com.bookk.core.data.eventstreaming.impl

import com.bookk.core.data.cache.test.InMemoryCacheClient
import com.bookk.core.data.eventstreaming.EventIdempotencyStorage
import com.bookk.core.data.repository.CacheEventIdempotencyStorage

fun inMemoryEventIdempotencyStorage(): EventIdempotencyStorage = CacheEventIdempotencyStorage(InMemoryCacheClient())
