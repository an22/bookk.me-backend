package com.bookk.appointments.domain.datasource

import kotlin.uuid.Uuid

interface AppointmentSubscriptionDataSource {
    suspend fun attachBusiness(businessId: Uuid)
    suspend fun detachBusiness(businessId: Uuid)
    suspend fun disableBusiness(businessId: Uuid)
}