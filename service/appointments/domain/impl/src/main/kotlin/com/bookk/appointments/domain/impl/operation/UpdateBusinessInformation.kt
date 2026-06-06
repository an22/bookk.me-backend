package com.bookk.appointments.domain.impl.operation

import com.bookk.appointments.domain.datasource.AppointmentSubscriptionDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.server.business.client.api.event.BusinessEvent

internal class UpdateBusinessInformation(
    private val subscriptionDataSource: AppointmentSubscriptionDataSource,
    private val transactionManager: TransactionManager
) {

    suspend operator fun invoke(business: BusinessEvent.BusinessDTO) = transactionManager.transaction {
        subscriptionDataSource.updateBusinessInfo(business.id, business.name, business.address)
    }
}