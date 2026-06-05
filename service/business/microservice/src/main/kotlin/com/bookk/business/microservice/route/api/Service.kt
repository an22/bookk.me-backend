package com.bookk.business.microservice.route.api

import com.bookk.business.domain.api.service.entity.Service
import com.bookk.business.domain.api.service.operation.CreateService
import com.bookk.business.domain.api.service.operation.DeleteService
import com.bookk.business.domain.api.service.operation.GetServices
import com.bookk.business.domain.api.service.operation.UpdateService
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
import io.ktor.server.resources.put
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.routing.openapi.describe
import org.koin.ktor.ext.inject

fun Route.serviceCrud() {
    authenticate {
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
        }.describe {
            summary = "Create a new service"
            description = "Creates a new service offering that can be presented to the clients"
            tag("service")
            requestBody {
                required = true
                schema = jsonSchema<Service>()
                ContentType.Application.ProtoBuf()
            }
            responses {
                HttpStatusCode.OK {
                    description = "Created service entity"
                    schema = jsonSchema<Service>()
                    ContentType.Application.ProtoBuf()
                }
                HttpStatusCode.UnprocessableEntity {
                    description = buildString {
                        append(CreateService.Error.ServiceExist().asServerError().toString())
                        append("\n\n")
                        append(CreateService.Error.ValidationError().asServerError().toString())
                    }
                    schema = jsonSchema<SimpleServerError>()
                    ContentType.Application.ProtoBuf()
                }
            }
        }

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
        }.describe {
            summary = "Update service"
            description = "Updates a new service offering that can be presented to the clients"
            tag("service")
            requestBody {
                required = true
                schema = jsonSchema<Service>()
                ContentType.Application.ProtoBuf()
            }
            responses {
                HttpStatusCode.OK {
                    description = "Updated service entity"
                    schema = jsonSchema<Service>()
                    ContentType.Application.ProtoBuf()
                }
                HttpStatusCode.UnprocessableEntity {
                    description = buildString {
                        append(UpdateService.Error.ServiceExist().asServerError().toString())
                        append("\n\n")
                        append(UpdateService.Error.ValidationError().asServerError().toString())
                    }
                    schema = jsonSchema<SimpleServerError>()
                    ContentType.Application.ProtoBuf()
                }
            }
        }

        get<Api.Service> {
            val getServices by application.inject<GetServices>()

            call.respondWith(getServices(it.businessId))
        }.describe {
            summary = "Get all services"
            description = "Get all service offerings of a business with specific id"
            tag("service")
            responses {
                HttpStatusCode.OK {
                    description = "Created service entity"
                    schema = jsonSchema<List<Service>>()
                    ContentType.Application.ProtoBuf()
                }
            }
        }

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
        }.describe {
            summary = "Delete service offering"
            description = "Delete service offering"
            tag("service")
            responses {
                HttpStatusCode.NoContent {
                    description = "Service offering deleted"
                }
            }
        }
    }
}
