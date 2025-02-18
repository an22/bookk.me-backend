package com.book.auth.domain.api.error

object AuthErrorCodes {
    const val EMAIL_EXIST = 1
    const val INVALID_EMAIL_FORMAT = 2
    const val USER_ALREADY_EXIST = 3
    const val VERIFICATION_FAILED = 4
    const val ACCOUNT_CREATION_FAILED = 5
    const val CHALLENGE_WINDOW_EXPIRED = 6
    const val PASSKEY_OWNER_NOT_FOUND = 7
    const val INVALID_CREDENTIALS = 8
}