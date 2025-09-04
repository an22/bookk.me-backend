package com.book.core.service.di

import com.book.core.data.ExposedTransactionManager
import com.book.core.data.eventstreaming.EventIdempotencyStorage
import com.book.core.data.repository.CacheIdempotentResponseRepository
import com.book.core.domain.datasource.transaction.TransactionManager
import com.wolt.utils.ktor.idempotency.IdempotentResponseRepository
import org.koin.core.module.Module
import org.koin.dsl.binds
import org.koin.dsl.module

internal fun commonModule(): Module = module {
    single { CacheIdempotentResponseRepository(get()) } binds arrayOf(IdempotentResponseRepository::class, EventIdempotencyStorage::class)
    factory<TransactionManager> { ExposedTransactionManager() }
}