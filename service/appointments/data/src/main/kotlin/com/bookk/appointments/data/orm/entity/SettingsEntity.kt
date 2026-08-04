package com.bookk.appointments.data.orm.entity

import com.bookk.appointments.data.map.toWorkingDays
import com.bookk.appointments.data.orm.table.SettingsTable
import com.bookk.appointments.domain.api.entity.AppointmentSettings
import com.bookk.appointments.domain.api.entity.AppointmentSettingsUpdate
import com.bookk.appointments.domain.api.entity.DayOffRange
import com.bookk.appointments.domain.api.entity.WorkHour
import com.bookk.appointments.domain.api.entity.WorkingSchedule
import com.bookk.core.data.DecoratorUUIDEntityClass
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.dao.UUIDEntity
import java.util.UUID
import kotlin.time.Clock
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

internal class SettingsEntity(id: EntityID<UUID>) : UUIDEntity(id) {

    var business by AppointmentBusinessEntity referencedOn SettingsTable.businessId
    var automaticApproval by SettingsTable.automaticApproval
    var inBetweenBreakInMinutes by SettingsTable.inBetweenBreakInMinutes
    var appointmentNote by SettingsTable.appointmentNote
    var updatedAt by SettingsTable.updatedAt

    fun domain(): AppointmentSettings = AppointmentSettings(
        id = id.value.toKotlinUuid(),
        businessId = business.id.value.toKotlinUuid(),
        timeZone = TimeZone.of(business.timezone),
        schedule = WorkingSchedule(
            workingDays = business.workingDays.toWorkingDays(),
            workingHours = business.workingHours
                .map {
                    WorkHour(
                        dayOfWeek = DayOfWeek(it.dayOfWeek.toInt()),
                        from = it.startTime,
                        to = it.endTime
                    )
                }
                .groupBy { it.dayOfWeek }
        ),
        automaticApproval = automaticApproval,
        dayOffs = business.dayOffs.map { DayOffRange(it.startDate, it.endDate) },
        inBetweenBreakInMinutes = inBetweenBreakInMinutes,
        appointmentNote = appointmentNote,
    )

    companion object : DecoratorUUIDEntityClass<SettingsEntity>(SettingsTable) {
        fun new(settings: AppointmentSettings): SettingsEntity = new {
            business = AppointmentBusinessEntity[settings.businessId.toJavaUuid()]
            inBetweenBreakInMinutes = settings.inBetweenBreakInMinutes
            appointmentNote = settings.appointmentNote
            automaticApproval = settings.automaticApproval
        }

        fun findByBusinessId(businessId: UUID): SettingsEntity? =
            find { SettingsTable.businessId eq businessId }.singleOrNull()

        fun findByBusinessIdAndUpdate(update: AppointmentSettingsUpdate): SettingsEntity? =
            findByBusinessId(update.businessId.toJavaUuid())?.apply {
                inBetweenBreakInMinutes = update.inBetweenBreakInMinutes
                appointmentNote = update.appointmentNote
                automaticApproval = update.automaticApproval
                updatedAt = Clock.System.now()
            }
    }
}
