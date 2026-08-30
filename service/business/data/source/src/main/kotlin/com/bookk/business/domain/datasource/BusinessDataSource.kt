package com.bookk.business.domain.datasource

import com.bookk.business.domain.api.business.entity.Business
import com.bookk.business.domain.api.business.entity.BusinessUpdateModel
import com.bookk.business.domain.api.business.entity.UserBusinesses
import kotlinx.datetime.TimeZone
import kotlin.time.Instant
import kotlin.uuid.Uuid

interface BusinessDataSource {
    suspend fun createBusiness(userId: Uuid, name: String, currencyCode: String, timeZone: TimeZone): Business
    suspend fun updateBusiness(model: BusinessUpdateModel, updatedAt: Instant): Business
    suspend fun getBusinessById(id: Uuid): Business?
    suspend fun isBusinessExist(userId: Uuid): Boolean
    suspend fun deleteUserBusinesses(userId: Uuid): List<Uuid>
    suspend fun getDashboardBusiness(userId: Uuid): Business?
    suspend fun getUserBusinesses(userId: Uuid): UserBusinesses
    suspend fun deleteDayOffsInThePast()
    suspend fun getPermission(userId: Uuid, businessId: Uuid): Int?
    suspend fun setUserPermissions(userId: Uuid, businessId: Uuid, permission: Int)
    suspend fun deleteUserPermissions(userId: Uuid)
}