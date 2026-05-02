package com.bookk.business.microservice.route.api

import com.bookk.business.domain.api.entity.Client
import com.bookk.business.domain.api.operation.CreateClient
import com.bookk.business.domain.api.operation.DeleteClient
import com.bookk.business.domain.api.operation.GetClients
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
         * Create client
         * @description Create new client within business
         * @security jwt
         * @tag *business
         * @request application/protobuf [Client]
         * @response 200 application/protobuf [Client] Created client entity
         * @response 422 application/protobuf [CreateClient.Error.ClientExist]
         * @response 422 application/protobuf [CreateClient.Error.ClientValidationError]
         */
        post<Api.Business.Id.Clients> {
            val body = call.receive<Client>()
            val createClient by application.inject<CreateClient>()

            call.respondWith(
                createClient(
                    businessId = it.parent.id,
                    client = body
                )
            )
        }

        /**
         * Get clients
         * @description Get clients list for specific business
         * @security jwt
         * @tag *business
         * @response 200 application/protobuf [kotlin.collections.List<Client>] Created client entity
         */
        get<Api.Business.Id.Clients> {
            val getClients by application.inject<GetClients>()

            call.respondWith(getClients(it.parent.id))
        }

        /**
         * Delete client
         * @description Get clients list for specific business
         * @security jwt
         * @tag *business
         * @response 204 Entity deleted
         */
        delete<Api.Business.Id.Clients.Id> {
            val deleteClient by application.inject<DeleteClient>()

            call.respondWith(deleteClient(it.parent.parent.id, it.id))
        }
    }
}