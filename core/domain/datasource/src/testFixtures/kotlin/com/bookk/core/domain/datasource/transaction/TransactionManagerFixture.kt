package com.bookk.core.domain.datasource.transaction

import io.mockk.coEvery

fun TransactionManager.mockTransaction() {
    coEvery { transaction<Any>(any()) } coAnswers {
        try {
            Result.success(firstArg<suspend () -> Any>().invoke())
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }
}
