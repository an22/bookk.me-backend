package com.book.core.data

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import java.util.UUID

abstract class R2dbcUUIDEntityClass<out E : UUIDEntity>(
    table: IdTable<UUID>,
    entityType: Class<E>? = null,
    entityCtor: ((EntityID<UUID>) -> E)? = null
) : UUIDEntityClass<E>(table, entityType, entityCtor) {

    fun wrapRowR2dbc(row: ResultRow): E {
        val entity = wrapR2dbc(row[table.id], row)
        if (entity._readValues == null) {
            entity._readValues = row
        }

        return entity
    }

    protected fun wrapR2dbc(id: EntityID<UUID>, row: ResultRow?): E {
        return createInstance(id, row)
    }
}

