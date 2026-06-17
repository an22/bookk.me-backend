package com.bookk.business.microservice.route.api

import com.bookk.business.domain.api.client.entity.ClientRemote
import com.bookk.business.domain.api.client.entity.toDomain
import com.bookk.business.domain.api.client.operation.CreateClient
import com.bookk.business.domain.api.client.operation.DeleteClient
import com.bookk.business.domain.api.client.operation.GetClients
import com.bookk.business.microservice.route.BusinessRouting.Api
import com.bookk.core.service.enity.respondWith
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.resources.delete
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import org.koin.ktor.ext.inject

fun Route.clientCrud() {
    authenticate {
        /**
         * Summary: Create client
         * Description: Create new client within business
         * Tag: business
         * Security: jwt
         * Body: application/x-protobuf [com.bookk.business.domain.api.client.entity.ClientRemote]
         * Response: 200 application/x-protobuf [com.bookk.business.domain.api.client.entity.Client] Created client entity
         * Response: 422 application/x-protobuf [com.bookk.core.domain.entity.SimpleServerError] Create client errors<br>BUSINESS_CLIENT_EXISTS (Code 200004) Client with this phone already exists<br>BUSINESS_CLIENT_NAME_VALIDATION_ERROR (Code 200005) Client name or last name is too long
         */
        post<Api.Clients> {
            val body = call.receive<ClientRemote>()
            val createClient by application.inject<CreateClient>()

            call.respondWith(
                createClient(
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
         * Response: 200 application/x-protobuf [kotlin.collections.List<com.bookk.business.domain.api.client.entity.Client>] List of clients
         */
        get<Api.Clients> {
            val getClients by application.inject<GetClients>()

            call.respondWith(getClients(it.businessId))
        }

        /**
         * Summary: Delete client
         * Description: Delete client by id
         * Tag: business
         * Security: jwt
         * Response: 204 application/x-protobuf Entity deleted
         */
        delete<Api.Clients.Id> {
            val deleteClient by application.inject<DeleteClient>()

            call.respondWith(deleteClient(it.parent.businessId, it.id))
        }
    }
}
