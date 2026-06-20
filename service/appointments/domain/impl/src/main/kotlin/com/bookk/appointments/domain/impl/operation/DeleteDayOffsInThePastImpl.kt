package com.bookk.appointments.domain.impl.operation

import com.bookk.appointments.domain.api.operation.DeleteDayOffsInThePast
import com.bookk.appointments.domain.datasource.AppointmentSettingsDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager

internal class DeleteDayOffsInThePastImpl(
    private val appointmentSettingsDataSource: AppointmentSettingsDataSource,
    private val transactionManager: TransactionManager
) : DeleteDayOffsInThePast {
    override suspend fun invoke(): Result<Unit> = transactionManager.transaction {
        appointmentSettingsDataSource.deleteDayOffsInThePast()
    }
}
