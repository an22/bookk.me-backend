package com.book.core.domain.operation

interface SuspendOperation <in Param, out Result> {
    suspend fun call(params: Param): Result
}