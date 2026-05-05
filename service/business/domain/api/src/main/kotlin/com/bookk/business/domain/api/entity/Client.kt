package com.bookk.business.domain.api.entity

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
class ClientRemote(
    val id: Uuid,
    val name: String,
    val lastName: String,
    val phone: String,
    val userId: Uuid?
)

sealed interface Client {
    val id: Uuid
    val name: String
    val lastName: String
    val phone: String

    data class Detached(
        override val id: Uuid,
        override val name: String,
        override val lastName: String,
        override val phone: String
    ) : Client

    data class Integrated(
        override val id: Uuid,
        override val name: String,
        override val lastName: String,
        override val phone: String,
        val userId: Uuid
    ) : Client
}

fun Client.toRemote(): ClientRemote {
    return ClientRemote(
        id = id,
        name = name,
        lastName = lastName,
        phone = phone,
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
        )
    } else {
        Client.Integrated(
            id = id,
            name = name,
            lastName = lastName,
            phone = phone,
            userId = userId
        )
    }
}