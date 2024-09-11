package com.book.user.domain.api.util

import java.security.MessageDigest

fun createPasswordHash(password: String): String {
    return MessageDigest.getInstance("SHA-256")
        .digest(password.toByteArray())
        .fold("") { str, it -> str + "%02x".format(it) }
}