package com.bookk.business.microservice.route.api

import com.bookk.business.domain.api.service.entity.ServiceGroup
import com.bookk.business.domain.api.service.operation.CreateServiceGroup
import com.bookk.business.domain.api.service.operation.DeleteServiceGroup
import com.bookk.business.domain.api.service.operation.GetServiceGroups
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
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import org.koin.ktor.ext.inject

fun Route.serviceGroupCrud() {
    authenticate {
        /**
         * Summary: Create a new service group
         * Description: Creates a new service group that can be presented to the clients
         * Tag: service_group
         * Security: jwt
         * RequestBody: application/x-protobuf [com.bookk.business.domain.api.service.entity.ServiceGroup]
         * Response: 200 application/x-protobuf [com.bookk.business.domain.api.service.entity.ServiceGroup] Created service entity
         * Response: 422 application/x-protobuf [com.bookk.core.domain.entity.SimpleServerError] Create service group errors
         */
        post<Api.ServiceGroup> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val body = call.receive<ServiceGroup>()
            val createServiceGroup by application.inject<CreateServiceGroup>()

            if (it.businessId != body.businessId) {
                call.respond(HttpStatusCode.BadRequest, "Bad request")
            } else {
                call.respondWith(
                    createServiceGroup(
                        requestUserId = principal.userId,
                        service = body
                    )
                )
            }
        }

        /**
         * Summary: Get all service groups
         * Description: Get all service groups of a business with specific id
         * Tag: service_group
         * Security: jwt
         * Response: 200 application/x-protobuf [kotlin.collections.List<com.bookk.business.domain.api.service.entity.ServiceGroup>] List of service groups
         */
        get<Api.ServiceGroup> {
            val getGroups by application.inject<GetServiceGroups>()

            call.respondWith(getGroups(it.businessId))
        }

        /**
         * Summary: Delete service group
         * Description: Delete service group with all services that belongs to it
         * Tag: service_group
         * Security: jwt
         * Response: 204 application/x-protobuf Service group deleted
         */
        delete<Api.ServiceGroup.Id> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val deleteGroup by application.inject<DeleteServiceGroup>()

            call.respondWith(
                deleteGroup(
                    requestUserId = principal.userId,
                    businessId = it.parent.businessId,
                    id = it.id
                )
            )
        }
    }
}
