package library.permissions

import com.bookk.core.domain.entity.Error

enum class ObjectPermission(val type: Int) {
    READ(1),
    WRITE(2),
    OWNER(100);

    operator fun compareTo(type: Int): Int {
        return this.type.compareTo(type)
    }
}

operator fun Int.compareTo(other: ObjectPermission): Int {
    return this.compareTo(other.type)
}

fun Int.assert(permission: ObjectPermission) {
    if (this < permission) throw Error.OperationNotAllowed()
}