package com.book.core.domain.entity

open class BusinessError(val code: Int, errorMessage: String? = null) : Throwable() {

    val errorMessage by lazy(LazyThreadSafetyMode.NONE) { errorMessage ?: message }

    protected fun readResolve(): Any = this
}