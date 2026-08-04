package com.bookk.business.domain.impl.operation.business

import com.bookk.business.domain.api.business.entity.Business
import com.bookk.business.domain.api.business.entity.BusinessUpdateModel
import com.bookk.business.domain.api.business.operation.UpdateBusiness
import com.bookk.business.domain.datasource.BusinessDataSource
import com.bookk.core.data.eventstreaming.StandardEventProducer
import com.bookk.core.data.eventstreaming.send
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.server.business.client.api.event.BusinessEvent
import library.permissions.ObjectPermission
import library.permissions.assert
import kotlin.time.Clock
import kotlin.uuid.Uuid

internal class UpdateBusinessImpl(
    private val businessDataSource: BusinessDataSource,
    private val transactionManager: TransactionManager,
    private val eventProducer: StandardEventProducer,
) : UpdateBusiness {
    override suspend fun invoke(requestUserId: Uuid, businessUpdateModel: BusinessUpdateModel): Result<Unit> =
        transactionManager.transaction {
            businessDataSource.getPermission(requestUserId, businessUpdateModel.id).assert(ObjectPermission.EDIT)
            businessUpdateModel.schedule?.let { update ->
                if (update.workingSchedule.list().any { it.isActive && it.workingTime.isEmpty() }) {
                    throw UpdateBusiness.Error.ActiveDayWithoutWorkHours()
                }
                if (update.dayOffs.any { it.start >= it.end }) {
                    throw UpdateBusiness.Error.InvalidDayOffRange()
                }
            }
            val name = businessUpdateModel.name?.take(Business.MAX_NAME_LENGTH)
            val description = businessUpdateModel.description?.take(Business.MAX_DESCRIPTION_LENGTH)
            val currencyCode = businessUpdateModel.currencyCode?.take(Business.MAX_CURRENCY_CODE)
            val address = businessUpdateModel.address?.take(Business.MAX_ADDRESS_LENGTH)
            val socials = businessUpdateModel.socials?.map {
                it.copy(value = it.value?.take(Business.MAX_SOCIAL_LENGTH))
            }
            val updatedModel = businessUpdateModel.copy(
                name = name,
                description = description,
                currencyCode = currencyCode,
                address = address,
                socials = socials
            )
            businessDataSource.updateBusiness(updatedModel).also { business ->
                eventProducer.send(
                    BusinessEvent.Updated(
                        business = business.toDto(),
                        updatedAt = Clock.System.now()
                    )
                )
            }
        }

    private fun Business.toDto() = BusinessEvent.BusinessDTO(
        id = id,
        name = name,
        address = address,
        timeZone = timeZone,
        schedule = BusinessEvent.ScheduleDTO(
            workingDays = schedule.activeDays(),
            workingHours = schedule.list()
                .flatMap { it.workingTime }
                .map { BusinessEvent.WorkHourDTO(it.dayOfWeek, it.from, it.to) },
            dayOffs = dayOffs.map { BusinessEvent.DayOffDTO(it.start, it.end) }
        )
    )
}
