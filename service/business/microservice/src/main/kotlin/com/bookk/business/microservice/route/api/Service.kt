package com.bookk.business.microservice.route.api

import com.bookk.business.domain.api.service.entity.Service
import com.bookk.business.domain.api.service.operation.CreateService
import com.bookk.business.microservice.route.BusinessRouting.Api
import com.bookk.core.service.enity.respondWith
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.resources.post
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import org.koin.ktor.ext.inject

fun Route.serviceCrud() {
    authenticate {
        /**
         * Create client
         * @description Create new client within business
         * @security jwt
         * @tag business
         * @request application/protobuf [Service]
         * @response 200 application/protobuf [Service] Created client entity
         * @response 422 application/protobuf [CreateService.Error.ServiceExist]
         * @response 422 application/protobuf [CreateService.Error.ValidationError]
         */
        post<Api.Service> {
            val body = call.receive<Service>()
            val createService by application.inject<CreateService>()

            call.respondWith(createService(service = body))
        }
    }
}