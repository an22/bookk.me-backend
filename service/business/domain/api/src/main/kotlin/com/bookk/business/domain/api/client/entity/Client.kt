package com.bookk.business.domain.api.client.entity

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
class ClientRemote(
    val id: Uuid,
    val name: String,
    val lastName: String,
    val phone: String?,
    val email: String?,
    val userId: Uuid?
)

sealed interface Client {
    val id: Uuid
    val name: String
    val lastName: String
    val phone: String?
    val email: String?

    data class Detached(
        override val id: Uuid,
        override val name: String,
        override val lastName: String,
        override val phone: String?,
        override val email: String?
    ) : Client

    data class Integrated(
        override val id: Uuid,
        override val name: String,
        override val lastName: String,
        override val phone: String?,
        override val email: String?,
        val userId: Uuid
    ) : Client
}

fun Client.toRemote(): ClientRemote {
    return ClientRemote(
        id = id,
        name = name,
        lastName = lastName,
        phone = phone,
        email = email,
        userId = (this as? Client.Integrated)?.userId
    )
}

fun ClientRemote.toDomain(): Client {
    return if (userId == null) {
        Client.Detached(
            id = id,
            name = name,
            lastName = lastName,
            phone = phone,
            email = email
        )
    } else {
        Client.Integrated(
            id = id,
            name = name,
            lastName = lastName,
            phone = phone,
            email = email,
            userId = userId
        )
    }
}