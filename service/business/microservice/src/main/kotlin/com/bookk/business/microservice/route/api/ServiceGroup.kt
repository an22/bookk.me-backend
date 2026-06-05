package com.bookk.business.microservice.route.api

import com.bookk.business.domain.api.service.entity.ServiceGroup
import com.bookk.business.domain.api.service.operation.CreateServiceGroup
import com.bookk.business.domain.api.service.operation.DeleteServiceGroup
import com.bookk.business.domain.api.service.operation.GetServiceGroups
import com.bookk.business.microservice.route.BusinessRouting.Api
import com.bookk.core.domain.entity.SimpleServerError
import com.bookk.core.domain.entity.asServerError
import com.bookk.core.service.auth.AppPrincipal
import com.bookk.core.service.enity.respondWith
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.openapi.jsonSchema
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.resources.delete
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.routing.openapi.describe
import org.koin.ktor.ext.inject

fun Route.serviceGroupCrud() {
    authenticate {
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
        }.describe {
            summary = "Create a new service group"
            description = "Creates a new service group that can be presented to the clients"
            tag("service_group")
            requestBody {
                required = true
                schema = jsonSchema<ServiceGroup>()
                ContentType.Application.ProtoBuf()
            }
            responses {
                HttpStatusCode.OK {
                    description = "Created service entity"
                    schema = jsonSchema<ServiceGroup>()
                    ContentType.Application.ProtoBuf()
                }
                HttpStatusCode.UnprocessableEntity {
                    description = buildString {
                        append(CreateServiceGroup.Error.ServiceGroupExist().asServerError().toString())
                        append("\n\n")
                        append(CreateServiceGroup.Error.ValidationError().asServerError().toString())
                    }
                    schema = jsonSchema<SimpleServerError>()
                    ContentType.Application.ProtoBuf()
                }
            }
        }

        get<Api.ServiceGroup> {
            val getGroups by application.inject<GetServiceGroups>()

            call.respondWith(getGroups(it.businessId))
        }.describe {
            summary = "Get all service groups"
            description = "Get all service groups of a business with specific id"
            tag("service_group")
            responses {
                HttpStatusCode.OK {
                    description = "Created service entity"
                    schema = jsonSchema<List<ServiceGroup>>()
                    ContentType.Application.ProtoBuf()
                }
            }
        }

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
        }.describe {
            summary = "Delete service group"
            description = "Delete service group with all services that belongs to it"
            tag("service_group")
            responses {
                HttpStatusCode.NoContent {
                    description = "Group deleted"
                }
            }
        }
    }
}