package com.bookk.business.data.orm.table

import com.bookk.business.domain.api.employee.entity.EmployeeInvitationStatus
import com.bookk.core.data.database.BaseUUIDTable
import org.jetbrains.exposed.v1.core.ReferenceOption

object EmployeeInvitationTable : BaseUUIDTable("employee_invitation") {
    val businessId = reference("business_id", BusinessTable, onDelete = ReferenceOption.CASCADE).index()
    val userId = uuid("user_id").index()
    val invitedBy = uuid("invited_by")
    val name = varchar("name", 512)
    val lastName = varchar("lastname", 512)
    val phone = varchar("phone", 512).nullable()
    val email = varchar("email", 512).nullable()
    val status = enumeration("status", EmployeeInvitationStatus::class)

    init {
        index(isUnique = true, businessId, userId)
    }
}
