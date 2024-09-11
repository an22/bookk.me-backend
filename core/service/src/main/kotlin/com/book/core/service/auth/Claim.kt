package com.book.core.service.auth

enum class Claim(val key: String) {
    ID("id"),
    USERNAME("username"),
    ROLE("role"),
    DEVICE_ID("device_id")
}