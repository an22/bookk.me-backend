package com.bookk.business.microservice.route.api

import com.bookk.business.domain.api.business.entity.BusinessUpdateModel
import com.bookk.business.domain.api.business.operation.CreateBusiness
import com.bookk.business.domain.api.business.operation.GetBusinessById
import com.bookk.business.domain.api.business.operation.GetUserBusinesses
import com.bookk.business.domain.api.business.operation.UpdateBusiness
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
         * Summary: Create business
         * Description: Create new business with specific name
         * Tag: business
         * Security: jwt
         * Body: application/x-protobuf [com.bookk.business.microservice.route.api.BusinessCreateRequest]
         * Response: 422 application/x-protobuf [com.bookk.core.domain.entity.SimpleServerError] Create business errors:
         *  - BUSINESS_ALREADY_EXIST (Code 200001): Business already exist
         *  - BUSINESS_NAME_VALIDATION_ERROR (Code 200002): Business name invalid
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
         * Summary: Update business
         * Description: Partially update business
         * Tag: business
         * Security: jwt
         * Body: application/x-protobuf [com.bookk.business.domain.api.business.entity.BusinessUpdateModel] Non-null fields will be updated
         * Response: 204 application/x-protobuf No content
         */
        put<Api.Business.Id> {
            val body = call.receive<BusinessUpdateModel>()
            val updateBusiness by application.inject<UpdateBusiness>()

            call.respondWith(updateBusiness(body))
        }
        /**
         * Summary: Get dashboard business info
         * Description: Get all user business and dashboard business id. Dashboard business is a business that user selected to be displayed as main business
         * Tag: business
         * Security: jwt
         * Response: 200 application/x-protobuf [com.bookk.business.domain.api.business.entity.UserBusinesses] User business info
         */
        get<Api.Business> { path ->
            val getUserBusinesses by application.inject<GetUserBusinesses>()
            val principal = requireNotNull(call.principal<AppPrincipal>())

            call.respondWith(getUserBusinesses(userId = principal.userId))
        }
        /**
         * Summary: Get business by id
         * Tag: business
         * Security: jwt
         * Response: 200 application/x-protobuf [com.bookk.business.domain.api.business.entity.Business] Business info
         */
        get<Api.Business.Id> { path ->
            val getBusinessById by application.inject<GetBusinessById>()

            call.respondWith(getBusinessById(id = path.id))
        }
    }
}
