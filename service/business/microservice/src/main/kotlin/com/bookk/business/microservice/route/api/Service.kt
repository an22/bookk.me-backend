package com.bookk.business.microservice.route.api

import com.bookk.business.domain.api.service.entity.Service
import com.bookk.business.domain.api.service.operation.CreateService
import com.bookk.business.domain.api.service.operation.DeleteService
import com.bookk.business.domain.api.service.operation.GetServices
import com.bookk.business.domain.api.service.operation.UpdateService
import com.bookk.business.microservice.route.BusinessRouting.Api
import com.bookk.core.service.auth.AppPrincipal
import com.bookk.core.service.enity.respondWith
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.resources.delete
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import org.koin.ktor.ext.inject

fun Route.serviceCrud() {
    authenticate {
        /**
         * Summary: Create a new service
         * Description: Creates a new service offering that can be presented to the clients
         * Tag: service
         * Security: jwt
         * Body: application/x-protobuf [com.bookk.business.domain.api.service.entity.Service]
         * Response: 200 application/x-protobuf [com.bookk.business.domain.api.service.entity.Service] Created service entity
         * Response: 422 application/x-protobuf [com.bookk.core.domain.entity.SimpleServerError] Create service errors:
         *  - BUSINESS_SERVICE_EXISTS (Code 200007): Service with this name already exists
         *  - BUSINESS_SERVICE_NAME_VALIDATION_ERROR (Code 200008): Invalid service name
         */
        post<Api.Service> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val body = call.receive<Service>()
            val createService by application.inject<CreateService>()
            if (it.businessId != body.businessId) {
                call.respond(HttpStatusCode.BadRequest, "Bad request")
            } else {
                call.respondWith(
                    createService(requestUserId = principal.userId, service = body)
                )
            }
        }

        /**
         * Summary: Update service
         * Description: Updates a new service offering that can be presented to the clients
         * Tag: service
         * Security: jwt
         * Body: application/x-protobuf [com.bookk.business.domain.api.service.entity.Service]
         * Response: 200 application/x-protobuf [com.bookk.business.domain.api.service.entity.Service] Updated service entity
         * Response: 422 application/x-protobuf [com.bookk.core.domain.entity.SimpleServerError] Update service errors:
         *  - BUSINESS_SERVICE_EXISTS (Code 200007): Service with this name already exists
         *  - BUSINESS_SERVICE_NAME_VALIDATION_ERROR (Code 200008): Invalid service name
         */
        put<Api.Service.Id> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val body = call.receive<Service>()
            val updateService by application.inject<UpdateService>()

            if (it.parent.businessId != body.businessId) {
                call.respond(HttpStatusCode.BadRequest, "Bad request")
            } else {
                call.respondWith(
                    updateService(requestUserId = principal.userId, service = body)
                )
            }
        }

        /**
         * Summary: Get all services
         * Description: Get all service offerings of a business with specific id
         * Tag: service
         * Security: jwt
         * Response: 200 application/x-protobuf [kotlin.collections.List<com.bookk.business.domain.api.service.entity.Service>] List of services
         */
        get<Api.Service> {
            val getServices by application.inject<GetServices>()

            call.respondWith(getServices(it.businessId))
        }

        /**
         * Summary: Delete service offering
         * Description: Delete service offering
         * Tag: service
         * Security: jwt
         * Response: 204 application/x-protobuf Service offering deleted
         */
        delete<Api.Service.Id> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val deleteService by application.inject<DeleteService>()

            call.respondWith(
                deleteService(
                    requestUserId = principal.userId,
                    businessId = it.parent.businessId,
                    id = it.id
                )
            )
        }
    }
}
