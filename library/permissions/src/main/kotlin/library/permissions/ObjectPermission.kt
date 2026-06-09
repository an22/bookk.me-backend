package library.permissions

import com.bookk.core.domain.entity.Error

enum class ObjectPermission(val int: Int) {
    READ(1),
    EDIT(2),
    OWNER(100);

    operator fun compareTo(type: Int): Int {
        return this.int.compareTo(type)
    }
}

operator fun Int.compareTo(other: ObjectPermission): Int {
    return this.compareTo(other.int)
}

fun Int?.assert(permission: ObjectPermission) {
    this ?: throw Error.OperationNotAllowed()
    if (this < permission) throw Error.OperationNotAllowed()
}