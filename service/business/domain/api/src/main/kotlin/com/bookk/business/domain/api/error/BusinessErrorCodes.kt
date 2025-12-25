package com.bookk.business.domain.api.error

object BusinessErrorCodes {
    private const val BASE = 200000

    const val BUSINESS_ALREADY_EXIST = BASE + 1
    const val BUSINESS_NAME_VALIDATION_ERROR = BASE + 2
    const val BUSINESS_NOT_FOUND = BASE + 3
}