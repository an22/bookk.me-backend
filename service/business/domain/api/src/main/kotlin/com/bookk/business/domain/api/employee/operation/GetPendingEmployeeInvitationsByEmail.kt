package com.bookk.business.domain.api.employee.operation

import com.bookk.business.domain.api.employee.entity.EmployeeInvitation
import com.bookk.business.domain.api.error.BusinessErrorCodes
import com.bookk.core.domain.entity.BusinessError
import io.ktor.http.HttpStatusCode

interface GetPendingEmployeeInvitationsByEmail {
    suspend operator fun invoke(email: String): Result<List<EmployeeInvitation>>

    sealed interface Error {
        class ValidationError : BusinessError(
            statusCode = HttpStatusCode.UnprocessableEntity.value,
            code = BusinessErrorCodes.BUSINESS_EMPLOYEE_INVITATION_VALIDATION_ERROR,
            message = "Invitation email is blank or invalid"
        ), Error
    }
}
