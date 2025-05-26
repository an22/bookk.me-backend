package com.book.business.microservice.route.api

import com.book.business.domain.api.entity.Business
import com.book.business.domain.api.operation.CreateBusiness
import com.book.business.domain.api.operation.GetBusinessById
import com.book.business.domain.api.operation.GetDashboardBusiness
import com.book.business.microservice.route.BusinessRouting.Api
import com.book.core.service.applyMediaType
import com.book.core.service.auth.AppPrincipal
import com.book.core.service.enity.respondWith
import io.bkbn.kompendium.core.metadata.GetInfo
import io.bkbn.kompendium.core.metadata.PostInfo
import io.bkbn.kompendium.resources.NotarizedResource
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject

@Serializable
class BusinessCreateRequest(
    val name: String
)

fun Route.businessCrud() {
    withCreateBusinessDocumentation()
    authenticate {
        post<Api.Business> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val body = call.receive<BusinessCreateRequest>()
            val createBusiness by application.inject<CreateBusiness>()

            call.respondWith(createBusiness(userId = principal.userId, name = body.name))
        }
        get<Api.Business> { path ->
            val getDashboardBusiness by application.inject<GetDashboardBusiness>()
            val principal = requireNotNull(call.principal<AppPrincipal>())

            call.respondWith(getDashboardBusiness(userId = principal.userId))
        }
        get<Api.Business.Id> { path ->
            val getBusinessById by application.inject<GetBusinessById>()

            call.respondWith(getBusinessById(id = path.id))
        }
    }
}

internal fun Route.withCreateBusinessDocumentation() {
    install(NotarizedResource<Api.Business>()) {
        tags = setOf("business")
        get = GetInfo.builder {
            summary("Get dashboard business")
            description("Get business that user selected to be displayed as main business")
            response {
                responseCode(HttpStatusCode.OK)
                responseType<Business>()
                description("Dashboard business")
            }
        }
        post = PostInfo.builder {
            summary("Create business")
            description("Create new business with specific name")
            request {
                applyMediaType()
                requestType<BusinessCreateRequest>()
                description("Preferred business name")
            }
            response {
                responseCode(HttpStatusCode.OK)
                responseType<Business>()
                description("Created business entity")
            }
        }
    }
}