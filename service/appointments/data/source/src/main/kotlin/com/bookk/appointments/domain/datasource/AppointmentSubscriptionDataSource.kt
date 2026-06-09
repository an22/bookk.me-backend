package com.bookk.appointments.domain.datasource

import com.bookk.appointments.domain.api.entity.BusinessSnapshot
import kotlin.uuid.Uuid

interface AppointmentSubscriptionDataSource {
    suspend fun getBusinessSnapshot(id: Uuid): BusinessSnapshot?
    suspend fun attachBusiness(snapshot: BusinessSnapshot)
    suspend fun updateBusiness(snapshot: BusinessSnapshot)
    suspend fun updateBusinessInfo(id: Uuid, name: String, address: String)
    suspend fun detachBusiness(businessId: Uuid)
    suspend fun disableBusiness(businessId: Uuid)
}