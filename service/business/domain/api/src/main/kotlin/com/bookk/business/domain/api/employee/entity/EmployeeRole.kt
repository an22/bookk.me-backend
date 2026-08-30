package com.bookk.business.domain.api.employee.entity

import kotlinx.serialization.Serializable
import library.permissions.ObjectPermission

@Serializable
enum class EmployeeRole {
    EMPLOYEE,
    MANAGER;

    fun toPermission(): ObjectPermission = when (this) {
        EMPLOYEE -> ObjectPermission.READ
        MANAGER -> ObjectPermission.EDIT
    }
}
