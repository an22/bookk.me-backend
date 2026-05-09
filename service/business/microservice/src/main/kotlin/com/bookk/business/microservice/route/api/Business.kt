package com.bookk.business.microservice.route.api

import com.bookk.business.domain.api.entity.Business
import com.bookk.business.domain.api.entity.BusinessUpdateModel
import com.bookk.business.domain.api.entity.UserBusinesses
import com.bookk.business.domain.api.operation.CreateBusiness
import com.bookk.business.domain.api.operation.GetBusinessById
import com.bookk.business.domain.api.operation.GetUserBusinesses
import com.bookk.business.domain.api.operation.UpdateBusiness
import com.bookk.business.microservice.route.BusinessRouting.Api
import com.bookk.core.service.auth.AppPrincipal
import com.bookk.core.service.enity.respondWith
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject

@Serializable
class BusinessCreateRequest(
    val name: String,
    val currencyCode: String
)

fun Route.businessCrud() {
    authenticate {
        /**
         * Create business
         * @description Create new business with specific name
         * @security jwt
         * @tag business
         * @request application/protobuf [BusinessCreateRequest]
         * @response 200 application/protobuf [Business] Created business entity
         */
        post<Api.Business> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val body = call.receive<BusinessCreateRequest>()
            val createBusiness by application.inject<CreateBusiness>()

            call.respondWith(
                createBusiness(
                    userId = principal.userId,
                    name = body.name,
                    currencyCode = body.currencyCode
                )
            )
        }
        /**
         * Update business
         * @description Partially update business
         * @security jwt
         * @tag business
         * @request application/protobuf [BusinessUpdateModel] Non-null fields will be updated
         * @response 204 No content
         */
        put<Api.Business.Id> {
            val body = call.receive<BusinessUpdateModel>()
            val updateBusiness by application.inject<UpdateBusiness>()

            call.respondWith(updateBusiness(body))
        }
        /**
         * Get dashboard business info
         * @description Get all user business and dashboard business id. Dashboard business is a business that user selected to be displayed as main business
         * @security jwt
         * @tag business
         * @response 200 application/protobuf [UserBusinesses] User business info
         */
        get<Api.Business> { path ->
            val getUserBusinesses by application.inject<GetUserBusinesses>()
            val principal = requireNotNull(call.principal<AppPrincipal>())

            call.respondWith(getUserBusinesses(userId = principal.userId))
        }
        /**
         * Get business by id
         * @security jwt
         * @tag business
         * @response 200 application/protobuf [Business] Business info
         */
        get<Api.Business.Id> { path ->
            val getBusinessById by application.inject<GetBusinessById>()

            call.respondWith(getBusinessById(id = path.id))
        }
    }
}