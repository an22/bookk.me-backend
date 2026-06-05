package com.bookk.business.domain.api.error

object BusinessErrorCodes {
    private const val BASE = 200000

    const val BUSINESS_ALREADY_EXIST = BASE + 1
    const val BUSINESS_NAME_VALIDATION_ERROR = BASE + 2
    const val BUSINESS_NOT_FOUND = BASE + 3

    const val BUSINESS_CLIENT_EXISTS = BASE + 4
    const val BUSINESS_CLIENT_NAME_VALIDATION_ERROR = BASE + 5
    const val BUSINESS_CLIENT_NOT_EXISTS = BASE + 6

    const val BUSINESS_SERVICE_EXISTS = BASE + 7
    const val BUSINESS_SERVICE_NAME_VALIDATION_ERROR = BASE + 8
    const val BUSINESS_SERVICE_NOT_EXISTS = BASE + 9

    const val BUSINESS_SERVICE_GROUP_EXISTS = BASE + 10
    const val BUSINESS_SERVICE_GROUP_VALIDATION_ERROR = BASE + 11
    const val BUSINESS_SERVICE_GROUP_NOT_EXISTS = BASE + 12
}