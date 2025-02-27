package com.book.user.domain.api.entity


data class ContactForm(
    val userId: Long,
    val text: String,
    val usageLogs: String?
) {

    val isTextOutOfBounds: Boolean
        get() = text.length > UShort.MAX_VALUE.toInt()

    val isLogsOutOfBounds: Boolean
        get() = usageLogs.orEmpty().length > UShort.MAX_VALUE.toInt()

    val isBoundCapRequired: Boolean
        get() = isTextOutOfBounds || isLogsOutOfBounds

    companion object {
        const val TEXT_UPPER_BOUND = UShort.MAX_VALUE
        const val LOGS_UPPER_BOUND = UShort.MAX_VALUE
    }
}