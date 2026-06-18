package com.bookk.auth.data.orm.table

import com.bookk.auth.domain.api.token.entity.SigningKeyStatus
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.Clock

internal object SigningKeyTable : UUIDTable("signing_key") {
    val publicKey = text("public_key")
    val privateKey = text("private_key")
    val status = enumeration("status", SigningKeyStatus::class).index()
    val createdAt = timestamp("created_at").clientDefault { Clock.System.now() }
    val retiredAt = timestamp("retired_at").nullable()
}
