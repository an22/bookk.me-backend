package com.bookk.appointments.domain.impl.operation

import com.bookk.appointments.domain.api.operation.DeleteDayOffsInThePast
import com.bookk.appointments.domain.datasource.AppointmentSubscriptionDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager

internal class DeleteDayOffsInThePastImpl(
    private val subscriptionDataSource: AppointmentSubscriptionDataSource,
    private val transactionManager: TransactionManager
) : DeleteDayOffsInThePast {
    override suspend fun invoke(): Result<Unit> = transactionManager.transaction {
        subscriptionDataSource.deleteDayOffsInThePast()
    }
}
