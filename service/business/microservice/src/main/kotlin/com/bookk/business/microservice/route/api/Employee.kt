package com.bookk.business.microservice.route.api

import com.bookk.business.domain.api.business.entity.BusinessResource
import com.bookk.business.domain.api.employee.entity.Employee
import com.bookk.business.domain.api.employee.entity.EmployeeUpdateModel
import com.bookk.business.domain.api.employee.operation.GetEmployees
import com.bookk.business.domain.api.employee.operation.SetEmployeePermissions
import com.bookk.business.domain.api.employee.operation.UpdateEmployee
import com.bookk.business.domain.impl.di.BusinessScope
import com.bookk.business.microservice.route.BusinessRouting.Api
import com.bookk.core.service.di.injectScoped
import com.bookk.core.service.enity.respondWith
import com.bookk.server.auth.client.AppPrincipal
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.openapi.jsonSchema
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.resources.get
import io.ktor.server.resources.put
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.routing.openapi.describe
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import library.permissions.ResourcePermission

@Serializable
internal class EmployeePermissionsRequest(
    @ProtoNumber(1) val grants: Map<BusinessResource, ResourcePermission>
)

fun Route.employeeCrud() {
    authenticate {
        /**
         * Summary: Get employees
         * Description: Returns all employees of the business, each with their own current view/update/delete grants for every business resource; only users who can view the employees resource may list them
         * Tag: employee
         * Security: jwt
         */
        get<Api.Employee> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val getEmployees by application.injectScoped<GetEmployees>(BusinessScope)

            call.respondWith(getEmployees(userId = principal.userId, businessId = it.businessId))
        }.describe {
            responses {
                response(HttpStatusCode.OK.value) {
                    schema = jsonSchema<List<Employee>>()
                    description = "List of employees"
                    ContentType.Application.ProtoBuf()
                }
            }
        }

        /**
         * Summary: Update employee
         * Description: Updates the employee profile, schedule and provided services. Permissions are not part of the body and are changed only through the set employee permission endpoint
         * Tag: employee
         * Security: jwt
         * Body: application/x-protobuf [com.bookk.business.domain.api.employee.entity.EmployeeUpdateModel]
         * Response: 200 application/x-protobuf [com.bookk.business.domain.api.employee.entity.Employee] Updated employee
         * Response: 404 application/x-protobuf [com.bookk.core.domain.entity.SimpleServerError] Employee is not found or the caller has no rights to edit it
         * Response: 422 application/x-protobuf [com.bookk.core.domain.entity.SimpleServerError] Update employee errors<br>BUSINESS_EMPLOYEE_VALIDATION_ERROR (200021) Invalid employee name, last name, phone or email<br>BUSINESS_EMPLOYEE_ACTIVE_DAY_WITHOUT_WORK_HOURS (200022) Active day must have at least one work hour<br>BUSINESS_EMPLOYEE_INVALID_DAY_OFF_RANGE (200023) Day off range start date must not be after end date
         * See: docs/operations/business/update-employee.md
         */
        put<Api.Employee.Id> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val body = call.receive<EmployeeUpdateModel>()
            val updateEmployee by application.injectScoped<UpdateEmployee>(BusinessScope)

            if (it.parent.businessId != body.businessId || it.id != body.id) {
                call.respond(HttpStatusCode.BadRequest, "Bad request")
            } else {
                call.respondWith(
                    updateEmployee(requestUserId = principal.userId, employee = body)
                )
            }
        }

        /**
         * Summary: Set employee permissions
         * Description: Grants or revokes view/update/delete access to one or more business resources for an employee in a single request. Resources not listed keep their current grants. The caller cannot grant a level of access they do not themselves hold on any listed resource, in which case nothing is changed. An empty map changes nothing and returns the employee as is
         * Tag: employee
         * Security: jwt
         * Body: application/x-protobuf [com.bookk.business.microservice.route.api.EmployeePermissionsRequest]
         * Response: 200 application/x-protobuf [com.bookk.business.domain.api.employee.entity.Employee] Employee with updated permissions
         * Response: 404 application/x-protobuf [com.bookk.core.domain.entity.SimpleServerError] Employee is not found or the caller has no rights to manage permissions
         * Response: 422 application/x-protobuf [com.bookk.core.domain.entity.SimpleServerError] Set employee permissions errors<br>BUSINESS_INSUFFICIENT_GRANT_PERMISSION (200027) Cannot grant a permission level you do not hold
         * See: docs/operations/business/set-employee-permissions.md
         */
        put<Api.Employee.Id.Permissions> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val body = call.receive<EmployeePermissionsRequest>()
            val setEmployeePermissions by application.injectScoped<SetEmployeePermissions>(BusinessScope)

            call.respondWith(
                setEmployeePermissions(
                    requestUserId = principal.userId,
                    businessId = it.parent.parent.businessId,
                    employeeId = it.parent.id,
                    grants = body.grants
                )
            )
        }
    }
}
