package com.bookk.appointments.data.orm.table

import com.bookk.core.data.database.BaseUUIDTable

object UserHasAppointmentPermissions : BaseUUIDTable("user_has_appointment_permissions") {
    val userId = uuid("userId")
    val businessId = uuid("businessId")
    val permission = integer("permission")

    init {
        index(true, userId, businessId)
    }
}