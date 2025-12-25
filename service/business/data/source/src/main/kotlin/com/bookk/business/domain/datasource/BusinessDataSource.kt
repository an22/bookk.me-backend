package com.bookk.business.domain.datasource

import com.bookk.business.domain.api.entity.Business
import com.bookk.business.domain.api.entity.BusinessUpdateModel
import com.bookk.business.domain.api.entity.UserBusinesses
import kotlin.uuid.Uuid

interface BusinessDataSource {
    suspend fun createBusiness(userId: Uuid, name: String, currencyCode: String): Business
    suspend fun updateBusiness(model: BusinessUpdateModel)
    suspend fun getBusinessById(id: Uuid): Business?
    suspend fun isBusinessExist(userId: Uuid): Boolean
    suspend fun deleteUserBusinesses(userId: Uuid)
    suspend fun getDashboardBusiness(userId: Uuid): Business?
    suspend fun getUserBusinesses(userId: Uuid): UserBusinesses
}