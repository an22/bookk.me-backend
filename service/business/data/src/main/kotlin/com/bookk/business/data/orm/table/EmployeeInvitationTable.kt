package com.bookk.business.data.orm.table

import com.bookk.business.domain.api.employee.entity.EmployeeInvitationStatus
import com.bookk.core.data.database.BaseUUIDTable
import org.jetbrains.exposed.v1.core.ReferenceOption

object EmployeeInvitationTable : BaseUUIDTable("employee_invitation") {
    val businessId = reference("business_id", BusinessTable, onDelete = ReferenceOption.CASCADE).index()
    val invitedBy = uuid("invited_by")
    val email = varchar("email", 512)
    val status = enumeration("status", EmployeeInvitationStatus::class)

    init {
        index(isUnique = true, businessId, email)
    }
}
