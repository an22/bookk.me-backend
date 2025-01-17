package com.book.core.service.di

import com.book.core.data.ExposedTransactionManager
import com.book.core.data.repository.CacheIdempotentResponseRepository
import com.book.core.domain.transaction.TransactionManager
import com.wolt.utils.ktor.idempotency.IdempotentResponseRepository
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun commonModule(): Module = module {
    factory<IdempotentResponseRepository> { CacheIdempotentResponseRepository(get()) }
    factory<TransactionManager> { ExposedTransactionManager() }
}