package com.bookk.notifications.domain.api.entity

import com.bookk.core.domain.entity.Language
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class Device(
    val id: Uuid,
    val authId: Uuid,
    val deviceUuid: Uuid,
    val userId: Uuid,
    val notificationToken: String?,
    val language: Language,
) {
    companion object {
        fun stub(
            id: Uuid = Uuid.random(),
            authId: Uuid = Uuid.random(),
            deviceId: Uuid = Uuid.random(),
            userId: Uuid = Uuid.random(),
            notificationToken: String? = null,
            language: Language = Language.EN,
        ) = Device(id, authId, deviceId, userId, notificationToken, language)
    }
}
