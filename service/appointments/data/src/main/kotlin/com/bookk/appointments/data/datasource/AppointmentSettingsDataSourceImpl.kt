package com.bookk.appointments.data.datasource

import com.bookk.appointments.data.orm.entity.DayOffEntity
import com.bookk.appointments.data.orm.entity.SettingsEntity
import com.bookk.appointments.data.orm.entity.WorkingHourEntity
import com.bookk.appointments.data.orm.table.AppointmentBusinessTable
import com.bookk.appointments.data.orm.table.DayOffsTable
import com.bookk.appointments.data.orm.table.SettingsTable
import com.bookk.appointments.domain.api.entity.AppointmentSettings
import com.bookk.appointments.domain.datasource.AppointmentSettingsDataSource
import com.bookk.core.data.DataSource
import com.bookk.core.domain.entity.Error
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.innerJoin
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.select
import kotlin.time.Clock
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
        SettingsEntity.find {
            SettingsTable.businessId eq businessId.toJavaUuid()
        }
            .singleOrNull()
            ?.domain()
    }

    override suspend fun getForUpdate(businessId: Uuid): AppointmentSettings? = dbQuery {
        SettingsEntity.find {
            SettingsTable.businessId eq businessId.toJavaUuid()
        }
            .forUpdate()
            .singleOrNull()
            ?.domain()
    }

    override suspend fun deleteDayOffsInThePast() = dbQuery {
        val now = Clock.System.now()
        val settingsIdsByTimeZone = SettingsTable
            .innerJoin(AppointmentBusinessTable, onColumn = { businessId }, otherColumn = { id })
            .select(SettingsTable.id, AppointmentBusinessTable.timeZone)
            .groupBy(
                keySelector = { it[AppointmentBusinessTable.timeZone] },
                valueTransform = { it[SettingsTable.id].value }
            )

        settingsIdsByTimeZone.forEach { (timeZone, settingsIds) ->
            val today = now.toLocalDateTime(TimeZone.of(timeZone)).date
            DayOffsTable.deleteWhere {
                DayOffsTable.settingsId.inList(settingsIds)
                    .and(DayOffsTable.endDate.less(today))
            }
        }
    }
}