package com.bookk.user.data.orm.entity

import com.bookk.core.data.DecoratorUUIDEntityClass
import com.bookk.user.data.orm.table.UserTable
import com.bookk.user.domain.api.entity.User
import com.bookk.user.domain.api.entity.UserEditModel
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UUIDEntity
import java.util.UUID
import kotlin.time.Instant
import kotlin.uuid.toKotlinUuid

class UserEntity(id: EntityID<UUID>) : UUIDEntity(id) {

    var name by UserTable.name
    var lastName by UserTable.lastName
    var email by UserTable.email
    var createdAt by UserTable.createdAt
    var updatedAt by UserTable.updatedAt

    fun domain(): User = User(
        id = id.value.toKotlinUuid(),
        name = name,
        lastName = lastName,
        email = email
    )

    companion object : DecoratorUUIDEntityClass<UserEntity>(UserTable) {
        fun applyEdit(id: UUID, model: UserEditModel, updatedAt: Instant): UserEntity? =
            findByIdAndUpdate(id) {
                model.firstName?.let { firstName -> it.name = firstName }
                model.lastName?.let { lastName -> it.lastName = lastName }
                model.email?.let { email -> it.email = email }
                it.updatedAt = updatedAt
            }
    }
}
