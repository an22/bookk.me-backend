package com.bookk.business.microservice.route.api

import com.bookk.business.domain.api.employee.entity.EmployeeInvitation
import com.bookk.business.domain.api.employee.operation.CreateEmployeeInvitation
import com.bookk.business.domain.api.employee.operation.GetEmployeeInvitations
import com.bookk.business.domain.api.employee.operation.JoinBusiness
import com.bookk.business.domain.api.employee.operation.RevokeEmployeeInvitation
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
import kotlinx.serialization.protobuf.ProtoNumber
import org.koin.ktor.ext.inject

@Serializable
internal class EmployeeInvitationRedeemRequest(
    @ProtoNumber(1) val code: String
)

fun Route.employeeInvitationCrud() {
    authenticate {
        /**
         * Summary: Create employee invitation
         * Description: Generates a new invite code the business owner can share with a future employee
         * Tag: employee
         * Security: jwt
         * Response: 200 application/x-protobuf [com.bookk.business.domain.api.employee.entity.EmployeeInvitation] Created invitation
         * Response: 404 application/x-protobuf [com.bookk.core.domain.entity.SimpleServerError] Business is not found or the caller has no rights to invite
         * See: docs/operations/business/create-employee-invitation.md
         */
        post<Api.EmployeeInvitation> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val createEmployeeInvitation by application.inject<CreateEmployeeInvitation>()

            call.respondWith(createEmployeeInvitation(requestUserId = principal.userId, businessId = it.businessId))
        }

        /**
         * Summary: Get employee invitations
         * Description: Returns all invitations in this business sent by the calling user, regardless of status
         * Tag: employee
         * Security: jwt
         */
        get<Api.EmployeeInvitation> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val getEmployeeInvitations by application.inject<GetEmployeeInvitations>()

            call.respondWith(getEmployeeInvitations(userId = principal.userId, businessId = it.businessId))
        }.describe {
            responses {
                response(HttpStatusCode.OK.value) {
                    schema = jsonSchema<List<EmployeeInvitation>>()
                    description = "Invitations sent by the calling user"
                    ContentType.Application.ProtoBuf()
                }
            }
        }

        /**
         * Summary: Join business
         * Description: Joins the business as an employee using an invite code shared by the owner
         * Tag: employee
         * Security: jwt
         * Body: application/x-protobuf [com.bookk.business.microservice.route.api.EmployeeInvitationRedeemRequest]
         * Response: 200 application/x-protobuf [com.bookk.business.domain.api.employee.entity.Employee] Created employee
         * Response: 404 application/x-protobuf [com.bookk.core.domain.entity.SimpleServerError] Invite code is unknown
         * Response: 422 application/x-protobuf [com.bookk.core.domain.entity.SimpleServerError] Join business errors<br>BUSINESS_EMPLOYEE_INVITATION_ALREADY_PROCESSED (200017) Invitation is already processed<br>BUSINESS_EMPLOYEE_EXISTS (200018) User is already an employee of this business
         * See: docs/operations/business/join-business.md
         */
        post<Api.RedeemEmployeeInvitation> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val body = call.receive<EmployeeInvitationRedeemRequest>()
            val joinBusiness by application.inject<JoinBusiness>()

            call.respondWith(joinBusiness(requestUserId = principal.userId, code = body.code))
        }

        /**
         * Summary: Revoke employee invitation
         * Description: Lets the business owner manually revoke a pending invitation before it is redeemed or expires
         * Tag: employee
         * Security: jwt
         * Response: 204 application/x-protobuf Invitation revoked
         * Response: 404 application/x-protobuf [com.bookk.core.domain.entity.SimpleServerError] Invitation is not found or the caller has no rights to revoke it
         * Response: 422 application/x-protobuf [com.bookk.core.domain.entity.SimpleServerError] Revoke invitation errors<br>BUSINESS_EMPLOYEE_INVITATION_ALREADY_PROCESSED (200017) Invitation is already processed
         * See: docs/operations/business/revoke-employee-invitation.md
         */
        post<Api.EmployeeInvitation.Revoke> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val revokeEmployeeInvitation by application.inject<RevokeEmployeeInvitation>()

            call.respondWith(
                revokeEmployeeInvitation(
                    requestUserId = principal.userId,
                    businessId = it.parent.businessId,
                    id = it.id
                )
            )
        }
    }
}
