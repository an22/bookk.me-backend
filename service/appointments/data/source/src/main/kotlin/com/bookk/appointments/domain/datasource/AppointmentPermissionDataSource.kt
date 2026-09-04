package com.bookk.appointments.domain.datasource

import kotlin.uuid.Uuid

interface AppointmentPermissionDataSource {
    suspend fun setPermissions(userId: Uuid, businessId: Uuid, permissions: Int)
    suspend fun getPermissions(userId: Uuid, businessId: Uuid): Int?
    suspend fun deleteForUser(userId: Uuid)
}