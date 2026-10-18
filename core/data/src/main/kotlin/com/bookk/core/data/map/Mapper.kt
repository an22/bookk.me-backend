package com.bookk.core.data.map

import com.bookk.core.domain.entity.Error
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import java.sql.SQLException
import java.sql.SQLIntegrityConstraintViolationException

private const val INTEGRITY_CONSTRAINT_VIOLATION_STATE_CLASS = "23"

fun Throwable.toDomain(): Error {
    return when (this) {
        is Error -> this
        is ExposedSQLException -> {
            if ((cause as? SQLException)?.isIntegrityConstraintViolation() == true) {
                Error.UniqueConstraintFailed(message.orEmpty(), this)
            } else {
                Error.DatabaseError(message.orEmpty(), this)
            }
        }
        is SQLException -> Error.DatabaseError(message.orEmpty(), this)

        else -> Error.UnknownError( this)
    }
}

private fun SQLException.isIntegrityConstraintViolation(): Boolean =
    this is SQLIntegrityConstraintViolationException ||
        sqlState?.startsWith(INTEGRITY_CONSTRAINT_VIOLATION_STATE_CLASS) == true
