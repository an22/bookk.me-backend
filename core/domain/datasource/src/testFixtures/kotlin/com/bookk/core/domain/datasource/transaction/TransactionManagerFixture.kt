package com.bookk.core.domain.datasource.transaction

import io.mockk.coEvery
import kotlin.coroutines.cancellation.CancellationException

fun TransactionManager.mockTransaction() {
    coEvery { transaction<Any>(any()) } coAnswers {
        try {
            Result.success(firstArg<suspend () -> Any>().invoke())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }
}
