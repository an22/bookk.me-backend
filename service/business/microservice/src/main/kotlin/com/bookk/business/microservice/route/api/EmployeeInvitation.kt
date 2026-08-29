package com.bookk.business.microservice.route.api

import com.bookk.business.domain.api.employee.entity.EmployeeInvitation
import com.bookk.business.domain.api.employee.entity.EmployeeInvitationStatus
import com.bookk.business.domain.api.employee.operation.ApproveEmployeeInvitation
import com.bookk.business.domain.api.employee.operation.CreateEmployeeInvitation
import com.bookk.business.domain.api.employee.operation.GetPendingEmployeeInvitations
import com.bookk.business.microservice.route.BusinessRouting.Api
import com.bookk.core.service.enity.respondWith
import com.bookk.server.auth.client.AppPrincipal
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.openapi.jsonSchema
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.routing.openapi.describe
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
internal class EmployeeInvitationRequest(
    val email: String
)

internal fun EmployeeInvitationRequest.toDomain(businessId: Uuid, invitedBy: Uuid) = EmployeeInvitation(
    id = Uuid.random(),
    businessId = businessId,
    invitedBy = invitedBy,
    email = email,
    status = EmployeeInvitationStatus.PENDING,
    createdAt = Instant.fromEpochMilliseconds(0)
)

fun Route.employeeInvitationCrud() {
    authenticate {
        /**
         * Summary: Invite user to become an employee
         * Description: Creates a pending invitation for a user to join the business as an employee
         * Tag: employee
         * Security: jwt
         * Body: application/x-protobuf [com.bookk.business.microservice.route.api.EmployeeInvitationRequest]
         * Response: 200 application/x-protobuf [com.bookk.business.domain.api.employee.entity.EmployeeInvitation] Created invitation
         * Response: 404 application/x-protobuf [com.bookk.core.domain.entity.SimpleServerError] Business is not found or the caller has no rights to edit it
         * Response: 422 application/x-protobuf [com.bookk.core.domain.entity.SimpleServerError] Create invitation errors<br>BUSINESS_EMPLOYEE_INVITATION_EXISTS (200015) Invitation for this user already exists<br>BUSINESS_EMPLOYEE_INVITATION_VALIDATION_ERROR (200016) Invitation email is blank or invalid<br>BUSINESS_EMPLOYEE_EXISTS (200018) User is already an employee of this business
         * See: docs/operations/business/create-employee-invitation.md
         */
        post<Api.EmployeeInvitation> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val body = call.receive<EmployeeInvitationRequest>()
            val createEmployeeInvitation by application.inject<CreateEmployeeInvitation>()

            call.respondWith(
                createEmployeeInvitation(
                    requestUserId = principal.userId,
                    invitation = body.toDomain(businessId = it.businessId, invitedBy = principal.userId)
                )
            )
        }

        /**
         * Summary: Get pending employee invitations
         * Description: Returns pending invitations in this business addressed to the calling user
         * Tag: employee
         * Security: jwt
         */
        get<Api.EmployeeInvitation> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val getPendingEmployeeInvitations by application.inject<GetPendingEmployeeInvitations>()

            call.respondWith(getPendingEmployeeInvitations(userId = principal.userId, businessId = it.businessId))
        }.describe {
            responses {
                response(HttpStatusCode.OK.value) {
                    schema = jsonSchema<List<EmployeeInvitation>>()
                    description = "Pending invitations addressed to the calling user"
                    ContentType.Application.ProtoBuf()
                }
            }
        }

        /**
         * Summary: Approve employee invitation
         * Description: Approves a pending invitation and creates the employee for the invited user
         * Tag: employee
         * Security: jwt
         * Response: 200 application/x-protobuf [com.bookk.business.domain.api.employee.entity.Employee] Created employee
         * Response: 404 application/x-protobuf [com.bookk.core.domain.entity.SimpleServerError] Invitation is not found or is addressed to another user
         * Response: 422 application/x-protobuf [com.bookk.core.domain.entity.SimpleServerError] Approve invitation errors<br>BUSINESS_EMPLOYEE_INVITATION_ALREADY_PROCESSED (200017) Invitation is already processed<br>BUSINESS_EMPLOYEE_EXISTS (200018) User is already an employee of this business
         * See: docs/operations/business/approve-employee-invitation.md
         */
        post<Api.EmployeeInvitation.Approve> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val approveEmployeeInvitation by application.inject<ApproveEmployeeInvitation>()

            call.respondWith(
                approveEmployeeInvitation(
                    requestUserId = principal.userId,
                    businessId = it.parent.businessId,
                    id = it.id
                )
            )
        }
    }
}
