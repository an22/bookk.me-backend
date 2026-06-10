package com.bookk.appointments.data.datasource

import com.bookk.appointments.data.orm.entity.DayOffEntity
import com.bookk.appointments.data.orm.entity.SettingsEntity
import com.bookk.appointments.data.orm.entity.WorkingHourEntity
import com.bookk.appointments.data.orm.table.DayOffsTable
import com.bookk.appointments.data.orm.table.SettingsTable
import com.bookk.appointments.data.orm.table.WorkingHoursTable
import com.bookk.appointments.domain.api.entity.AppointmentSettings
import com.bookk.appointments.domain.datasource.AppointmentSettingsDataSource
import com.bookk.core.data.DataSource
import com.bookk.core.domain.entity.Error
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

internal class AppointmentSettingsDataSourceImpl : DataSource(), AppointmentSettingsDataSource {
    override suspend fun create(settings: AppointmentSettings): AppointmentSettings = dbQuery {
        val settingsEntity = SettingsEntity.new(settings)
        WorkingHourEntity.batchInsert(settingsEntity.id.value, settings.workingHours)
        DayOffEntity.batchInsert(settingsEntity.id.value, settings.dayOffs)
        settingsEntity
            .apply { refresh() }
            .domain()
    }

    override suspend fun update(settings: AppointmentSettings): AppointmentSettings = dbQuery {
        val settingsEntity = SettingsEntity.findByIdAndUpdate(settings) ?: throw Error.NotFound()
        settingsEntity.flush()
        WorkingHourEntity.batchInsert(settingsEntity.id.value, settings.workingHours)
        DayOffEntity.batchInsert(settingsEntity.id.value, settings.dayOffs)
        settingsEntity
            .apply { refresh() }
            .domain()
    }

    override suspend fun get(businessId: Uuid): AppointmentSettings? = dbQuery {
        SettingsEntity.findById(businessId.toJavaUuid())
            ?.domain()
    }

    override suspend fun getForUpdate(businessId: Uuid): AppointmentSettings? = dbQuery {
        SettingsTable
            .join(
                otherTable = WorkingHoursTable,
                joinType = JoinType.INNER,
                onColumn = SettingsTable.id,
                otherColumn = WorkingHoursTable.settingsId
            )
            .join(
                otherTable = DayOffsTable,
                joinType = JoinType.INNER,
                onColumn = SettingsTable.id,
                otherColumn = DayOffsTable.settingsId,
            )
            .selectAll()
            .forUpdate()
            .where { SettingsTable.businessId eq businessId.toJavaUuid() }
            .singleOrNull()
            ?.let { SettingsEntity.wrapRow(it).domain() }
    }
}