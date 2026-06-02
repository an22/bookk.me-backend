package com.bookk.appointments.data.datasource

import com.bookk.appointments.domain.api.entity.AppointmentRequest
import com.bookk.appointments.domain.datasource.AppointmentRequestDataSource
import com.bookk.core.data.DataSource
import kotlin.uuid.Uuid

internal class AppointmentRequestDataSourceImpl : DataSource(), AppointmentRequestDataSource {
    override suspend fun get(id: Uuid): AppointmentRequest? {
        TODO("Not yet implemented")
    }

    override suspend fun create(request: AppointmentRequest): AppointmentRequest {
        TODO("Not yet implemented")
    }

    override suspend fun update(request: AppointmentRequest): AppointmentRequest {
        TODO("Not yet implemented")
    }

    override suspend fun delete(request: AppointmentRequest) {
        TODO("Not yet implemented")
    }
}