package com.bookk.business.domain.impl.operation.employee

import com.bookk.business.domain.api.employee.entity.EmployeeInvitation
import com.bookk.business.domain.api.employee.operation.GetPendingEmployeeInvitationsByEmail
import com.bookk.business.domain.api.employee.operation.GetPendingEmployeeInvitationsByEmail.Error
import com.bookk.business.domain.datasource.EmployeeInvitationDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import library.validation.EmailValidator

internal class GetPendingEmployeeInvitationsByEmailImpl(
    private val invitationDataSource: EmployeeInvitationDataSource,
    private val transactionManager: TransactionManager
) : GetPendingEmployeeInvitationsByEmail {
    override suspend fun invoke(email: String): Result<List<EmployeeInvitation>> {
        if (!EmailValidator.isValid(email)) {
            return Result.failure(Error.ValidationError())
        }
        return transactionManager.transaction {
            invitationDataSource.getPendingInvitationsByEmail(email)
        }
    }
}
