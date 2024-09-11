package com.book.core.domain.operation

interface BlockingOperation<in Param, Result> {
    fun call(params: Param): Result
}