package com.book.user.domain.api.event

import kotlinx.serialization.Serializable

sealed interface UserEvents {
    @Serializable
    class DeleteUserEvent(
        val userId: Long
    ) : UserEvents {
        companion object {
            const val TOPIC = "user.delete_user"
        }
    }
}