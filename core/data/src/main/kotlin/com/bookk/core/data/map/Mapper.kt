package com.bookk.core.data.map

import com.bookk.core.domain.entity.Error
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import java.sql.SQLException
import java.sql.SQLIntegrityConstraintViolationException

fun Throwable.toDomain(): Error {
    return when (this) {
        is ExposedSQLException -> {
            if (cause is SQLIntegrityConstraintViolationException) {
                Error.UniqueConstraintFailed(message.orEmpty(), this)
            } else {
                Error.DatabaseError(message.orEmpty(), this)
            }
        }
        is SQLException -> Error.DatabaseError(message.orEmpty(), this)

        else -> Error.UnknownError( this)
    }
}