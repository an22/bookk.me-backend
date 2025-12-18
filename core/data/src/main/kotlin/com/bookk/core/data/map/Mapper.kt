package com.bookk.core.data.map

import com.bookk.core.domain.entity.Error
import java.sql.SQLException

fun Throwable.toDomain(): Error {
    return when (this) {
        is SQLException -> Error.DatabaseError(message.orEmpty(), this)

        else -> Error.UnknownError( this)
    }
}