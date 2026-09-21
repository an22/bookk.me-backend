package com.bookk.business.microservice.route.api

import com.bookk.business.domain.api.employee.entity.Employee
import com.bookk.business.domain.api.employee.operation.GetEmployeePermissions
import com.bookk.business.domain.api.employee.operation.GetEmployees
import com.bookk.business.domain.api.employee.operation.SetEmployeePermission
import com.bookk.business.domain.api.employee.operation.UpdateEmployee
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
import io.ktor.server.resources.put
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.routing.openapi.describe
import library.permissions.ResourcePermission
import org.koin.ktor.ext.inject

fun Route.employeeCrud() {
    authenticate {
        /**
         * Summary: Get employees
         * Description: Returns all employees of the business; only users who can view the employees resource may list them
         * Tag: employee
         * Security: jwt
         */
        get<Api.Employee> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val getEmployees by application.inject<GetEmployees>()

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
         * Description: Updates the employee profile, schedule and provided services
         * Tag: employee
         * Security: jwt
         * Body: application/x-protobuf [com.bookk.business.domain.api.employee.entity.Employee]
         * Response: 200 application/x-protobuf [com.bookk.business.domain.api.employee.entity.Employee] Updated employee
         * Response: 404 application/x-protobuf [com.bookk.core.domain.entity.SimpleServerError] Employee is not found or the caller has no rights to edit it
         * Response: 422 application/x-protobuf [com.bookk.core.domain.entity.SimpleServerError] Update employee errors<br>BUSINESS_EMPLOYEE_VALIDATION_ERROR (200021) Invalid employee name, last name, phone or email<br>BUSINESS_EMPLOYEE_ACTIVE_DAY_WITHOUT_WORK_HOURS (200022) Active day must have at least one work hour<br>BUSINESS_EMPLOYEE_INVALID_DAY_OFF_RANGE (200023) Day off range start date must not be after end date
         * See: docs/operations/business/update-employee.md
         */
        put<Api.Employee.Id> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val body = call.receive<Employee>()
            val updateEmployee by application.inject<UpdateEmployee>()

            if (it.parent.businessId != body.businessId || it.id != body.id) {
                call.respond(HttpStatusCode.BadRequest, "Bad request")
            } else {
                call.respondWith(
                    updateEmployee(requestUserId = principal.userId, employee = body)
                )
            }
        }

        /**
         * Summary: Get employee permissions
         * Description: Returns the employee's current view/update/delete grants for every business resource
         * Tag: employee
         * Security: jwt
         * Response: 200 application/x-protobuf [com.bookk.business.domain.api.business.entity.BusinessPermissions] Employee permissions
         * Response: 404 application/x-protobuf [com.bookk.core.domain.entity.SimpleServerError] Employee is not found or the caller has no rights to view permissions
         */
        get<Api.Employee.Id.Permissions> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val getEmployeePermissions by application.inject<GetEmployeePermissions>()

            call.respondWith(
                getEmployeePermissions(
                    requestUserId = principal.userId,
                    businessId = it.parent.parent.businessId,
                    employeeId = it.parent.id
                )
            )
        }

        /**
         * Summary: Set employee permission
         * Description: Grants or revokes view/update/delete access to one business resource for an employee. The caller cannot grant a level of access they do not themselves hold
         * Tag: employee
         * Security: jwt
         * Body: application/x-protobuf [library.permissions.ResourcePermission]
         * Response: 200 application/x-protobuf [com.bookk.business.domain.api.business.entity.BusinessPermissions] Employee's updated permissions
         * Response: 404 application/x-protobuf [com.bookk.core.domain.entity.SimpleServerError] Employee is not found or the caller has no rights to manage permissions
         * Response: 422 application/x-protobuf [com.bookk.core.domain.entity.SimpleServerError] Set employee permission errors<br>BUSINESS_INSUFFICIENT_GRANT_PERMISSION (200027) Cannot grant a permission level you do not hold
         * See: docs/operations/business/set-employee-permission.md
         */
        put<Api.Employee.Id.Permission> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val body = call.receive<ResourcePermission>()
            val setEmployeePermission by application.inject<SetEmployeePermission>()

            call.respondWith(
                setEmployeePermission(
                    requestUserId = principal.userId,
                    businessId = it.parent.parent.businessId,
                    employeeId = it.parent.id,
                    resource = it.resource,
                    permission = body
                )
            )
        }
    }
}
