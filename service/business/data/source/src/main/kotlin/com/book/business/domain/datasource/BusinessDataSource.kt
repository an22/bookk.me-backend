package com.book.business.domain.datasource

import com.book.business.domain.api.entity.Business
import com.book.business.domain.api.entity.BusinessUpdateModel

interface BusinessDataSource {
    suspend fun createBusiness(userId: Long, name: String): Business
    suspend fun updateBusiness(model: BusinessUpdateModel)
    suspend fun getBusinessById(id: Long): Business?
    suspend fun isBusinessExist(userId: Long): Boolean
    suspend fun deleteUserBusinesses(userId: Long)
    suspend fun getDashboardBusiness(userId: Long): Business?
}