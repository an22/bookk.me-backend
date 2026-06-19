package com.bookk.appointments.data.orm.table

import com.bookk.core.data.database.BaseUUIDTable
import org.jetbrains.exposed.v1.core.ReferenceOption

object UserHasAppointmentPermissions : BaseUUIDTable("user_has_appointment_permissions") {
    val userId = uuid("userId")
    val businessId = reference("business_id", AppointmentBusinessTable.id, onDelete = ReferenceOption.CASCADE)
    val permission = integer("permission")

    init {
        index(true, userId, businessId)
    }
}