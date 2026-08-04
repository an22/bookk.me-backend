package com.bookk.appointments.data.orm.entity

import com.bookk.appointments.data.orm.table.SettingsTable
import com.bookk.appointments.domain.api.entity.AppointmentSettings
import com.bookk.appointments.domain.api.entity.AppointmentSettingsUpdate
import com.bookk.core.data.DecoratorUUIDEntityClass
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
        schedule = business.schedule(),
        automaticApproval = automaticApproval,
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

        fun findByBusinessIdAndUpdate(update: AppointmentSettingsUpdate): SettingsEntity? =
            findSingleByAndUpdate(op = SettingsTable.businessId eq update.businessId.toJavaUuid()) {
                it.inBetweenBreakInMinutes = update.inBetweenBreakInMinutes
                it.appointmentNote = update.appointmentNote
                it.automaticApproval = update.automaticApproval
                it.updatedAt = Clock.System.now()
            }
    }
}
