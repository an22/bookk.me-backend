package com.bookk.business.microservice.route.api

import com.bookk.business.domain.api.client.entity.ClientRemote
import com.bookk.business.domain.api.client.entity.toDomain
import com.bookk.business.domain.api.client.operation.CreateClient
import com.bookk.business.domain.api.client.operation.DeleteClient
import com.bookk.business.domain.api.client.operation.GetClients
import com.bookk.business.microservice.route.BusinessRouting.Api
import com.bookk.core.domain.entity.SimpleServerError
import com.bookk.core.service.enity.respondWith
import com.bookk.server.auth.client.AppPrincipal
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.openapi.jsonSchema
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.resources.delete
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.routing.openapi.describe
import org.koin.ktor.ext.inject

fun Route.clientCrud() {
    authenticate {
        /**
         * Summary: Create client
         * Description: Create new client within business
         * Tag: business
         * Security: jwt
         * Body: application/x-protobuf [com.bookk.business.domain.api.client.entity.ClientRemote]
         * Response: 200 application/x-protobuf [com.bookk.business.domain.api.client.entity.ClientRemote] Created client entity
         * Response: 404 application/x-protobuf [com.bookk.core.domain.entity.SimpleServerError] User is not allowed to create clients for this business
         * Response: 422 application/x-protobuf [com.bookk.core.domain.entity.SimpleServerError] Create client errors<br>BUSINESS_CLIENT_EXISTS (200004) Client with this phone already exists<br>BUSINESS_CLIENT_NAME_VALIDATION_ERROR (200005) Client name, last name or phone is invalid
         */
        post<Api.Clients> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val body = call.receive<ClientRemote>()
            val createClient by application.inject<CreateClient>()

            call.respondWith(
                createClient(
                    requestUserId = principal.userId,
                    businessId = it.businessId,
                    client = body.toDomain()
                )
            )
        }

        /**
         * Summary: Get clients
         * Description: Get clients list for specific business
         * Tag: business
         * Security: jwt
         */
        get<Api.Clients> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val getClients by application.inject<GetClients>()

            call.respondWith(getClients(requestUserId = principal.userId, businessId = it.businessId))
        }.describe {
            responses {
                response(HttpStatusCode.OK.value) {
                    schema = jsonSchema<List<ClientRemote>>()
                    description = "List of clients"
                    ContentType.Application.ProtoBuf()
                }
                response(HttpStatusCode.NotFound.value) {
                    schema = jsonSchema<SimpleServerError>()
                    description = "User is not allowed to read clients of this business"
                    ContentType.Application.ProtoBuf()
                }
            }
        }

        /**
         * Summary: Delete client
         * Description: Delete client by id
         * Tag: business
         * Security: jwt
         * Response: 204 application/x-protobuf Entity deleted
         * Response: 404 application/x-protobuf [com.bookk.core.domain.entity.SimpleServerError] Client not found or user is not allowed to delete it
         */
        delete<Api.Clients.Id> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val deleteClient by application.inject<DeleteClient>()

            call.respondWith(
                deleteClient(
                    requestUserId = principal.userId,
                    businessId = it.parent.businessId,
                    id = it.id
                )
            )
        }
    }
}
