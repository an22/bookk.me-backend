package com.bookk.appointments.data.datasource

import com.bookk.appointments.domain.api.entity.AppointmentSettings
import com.bookk.appointments.domain.datasource.AppointmentSettingsDataSource
import com.bookk.core.data.DataSource
import kotlin.uuid.Uuid

internal class AppointmentSettingsDataSourceImpl : DataSource(), AppointmentSettingsDataSource {
    override suspend fun create(settings: AppointmentSettings): AppointmentSettings {
        TODO("Not yet implemented")
    }

    override suspend fun update(settings: AppointmentSettings): AppointmentSettings {
        TODO("Not yet implemented")
    }

    override suspend fun get(businessId: Uuid): AppointmentSettings? {
        TODO("Not yet implemented")
    }

    override suspend fun getForUpdate(businessId: Uuid): AppointmentSettings? {

    }
}