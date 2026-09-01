package com.bookk.business.microservice.route.api

import com.bookk.business.domain.api.employee.entity.Employee
import com.bookk.business.domain.api.employee.entity.EmployeeRole
import com.bookk.business.domain.api.employee.operation.GetEmployees
import com.bookk.business.domain.api.employee.operation.PromoteEmployee
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
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.routing.openapi.describe
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import org.koin.ktor.ext.inject

@Serializable
internal class PromoteEmployeeRequest(
    @ProtoNumber(1) val role: EmployeeRole
)

fun Route.employeeCrud() {
    authenticate {
        /**
         * Summary: Get employees
         * Description: Returns all employees of the business; only the business owner can list employees
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
         * Summary: Promote employee
         * Description: Changes an employee's permission level to Employee (read-only) or Manager (edit)
         * Tag: employee
         * Security: jwt
         * Body: application/x-protobuf [com.bookk.business.microservice.route.api.PromoteEmployeeRequest]
         * Response: 204 application/x-protobuf Employee promoted
         * Response: 404 application/x-protobuf [com.bookk.core.domain.entity.SimpleServerError] Employee is not found or the caller has no rights to promote employees
         * See: docs/operations/business/promote-employee.md
         */
        post<Api.Employee.Promote> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val body = call.receive<PromoteEmployeeRequest>()
            val promoteEmployee by application.inject<PromoteEmployee>()

            call.respondWith(
                promoteEmployee(
                    requestUserId = principal.userId,
                    businessId = it.parent.businessId,
                    employeeId = it.id,
                    role = body.role
                )
            )
        }
    }
}
