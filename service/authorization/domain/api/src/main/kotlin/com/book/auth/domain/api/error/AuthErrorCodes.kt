package com.book.auth.domain.api.error

object AuthErrorCodes {
    private const val BASE = 0

    const val EMAIL_EXIST = BASE + 1
    const val INVALID_EMAIL_FORMAT = BASE + 2
    const val USER_ALREADY_EXIST = BASE + 3
    const val VERIFICATION_FAILED = BASE + 4
    const val ACCOUNT_CREATION_FAILED = BASE + 5
    const val CHALLENGE_WINDOW_EXPIRED = BASE + 6
    const val PASSKEY_OWNER_NOT_FOUND = BASE + 7
    const val INVALID_CREDENTIALS = BASE + 8
}