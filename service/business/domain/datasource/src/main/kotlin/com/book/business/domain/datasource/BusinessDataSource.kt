package com.book.business.domain.datasource

import com.book.business.domain.api.entity.Business

interface BusinessDataSource {
    suspend fun createBusiness(name: String): Business
    suspend fun getBusinessById(id: Long): Business?
    suspend fun isBusinessExist(userId: Long): Boolean
}