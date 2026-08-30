package com.bookk.business.domain.api.employee.operation

import com.bookk.business.domain.api.employee.entity.EmployeeInvitation
import com.bookk.business.domain.api.error.BusinessErrorCodes
import com.bookk.core.domain.entity.BusinessError
import io.ktor.http.HttpStatusCode
import kotlin.uuid.Uuid

interface CreateEmployeeInvitation {
    suspend operator fun invoke(requestUserId: Uuid, invitation: EmployeeInvitation): Result<EmployeeInvitation>

    sealed interface Error {
        class InvitationExist : BusinessError(
            statusCode = HttpStatusCode.UnprocessableEntity.value,
            code = BusinessErrorCodes.BUSINESS_EMPLOYEE_INVITATION_EXISTS,
            message = "Invitation for this user already exists"
        ), Error

        class ValidationError : BusinessError(
            statusCode = HttpStatusCode.UnprocessableEntity.value,
            code = BusinessErrorCodes.BUSINESS_EMPLOYEE_INVITATION_VALIDATION_ERROR,
            message = "Invitation email is blank or invalid"
        ), Error

        class EmployeeExist : BusinessError(
            statusCode = HttpStatusCode.UnprocessableEntity.value,
            code = BusinessErrorCodes.BUSINESS_EMPLOYEE_EXISTS,
            message = "User is already an employee of this business"
        ), Error
    }
}
