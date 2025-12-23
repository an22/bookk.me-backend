package com.bookk.core.data

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import java.util.UUID

//Will be handy after R2dbc drivers and exposed libs will be stable, for now it is extremely unstable
abstract class DecoratorUUIDEntityClass<out E : UUIDEntity>(
    table: IdTable<UUID>,
    entityType: Class<E>? = null,
    entityCtor: ((EntityID<UUID>) -> E)? = null
) : UUIDEntityClass<E>(table, entityType, entityCtor)

