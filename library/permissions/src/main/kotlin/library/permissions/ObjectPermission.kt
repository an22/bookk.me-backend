package library.permissions

import com.bookk.core.domain.entity.Error

enum class ObjectPermission(val int: Int) {
    NONE(0),
    READ(1),
    EDIT(2),
    OWNER(100);

    operator fun compareTo(type: Int): Int {
        return this.int.compareTo(type)
    }

    companion object {
        fun of(value: Int?): ObjectPermission = entries.firstOrNull { it.int == value } ?: NONE
    }
}

operator fun Int.compareTo(other: ObjectPermission): Int {
    return this.compareTo(other.int)
}

fun Int?.assert(permission: ObjectPermission) {
    this ?: throw Error.OperationNotAllowed()
    if (this < permission) throw Error.OperationNotAllowed()
}

fun ObjectPermission?.assert(permission: ObjectPermission) {
    this ?: throw Error.OperationNotAllowed()
    if (this.int < permission.int) throw Error.OperationNotAllowed()
}