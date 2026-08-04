package com.bookk.appointments.domain.impl.operation

import com.bookk.appointments.domain.api.entity.BusinessSnapshot
import com.bookk.appointments.domain.api.entity.DayOffRange
import com.bookk.appointments.domain.api.entity.WorkHour
import com.bookk.appointments.domain.api.entity.WorkingSchedule
import com.bookk.appointments.domain.datasource.AppointmentSubscriptionDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.server.business.client.api.event.BusinessEvent
import kotlin.time.Instant

internal class UpdateBusinessInformation(
    private val subscriptionDataSource: AppointmentSubscriptionDataSource,
    private val transactionManager: TransactionManager
) {

    suspend operator fun invoke(
        business: BusinessEvent.BusinessDTO,
        updatedAt: Instant
    ) = transactionManager.transaction {
        subscriptionDataSource.updateBusiness(
            snapshot = BusinessSnapshot(
                id = business.id,
                name = business.name,
                address = business.address,
                timeZone = business.timeZone,
                isEnabled = true,
                schedule = WorkingSchedule(
                    workingDays = business.schedule.workingDays,
                    workingHours = business.schedule.workingHours
                        .map { WorkHour(dayOfWeek = it.dayOfWeek, from = it.from, to = it.to) }
                        .groupBy { it.dayOfWeek }
                ),
                dayOffs = business.schedule.dayOffs.map { DayOffRange(start = it.start, end = it.end) }
            ),
            updatedAt = updatedAt
        )
    }
}
