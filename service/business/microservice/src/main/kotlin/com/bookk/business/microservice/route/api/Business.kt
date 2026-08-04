package com.bookk.business.microservice.route.api

import com.bookk.business.domain.api.business.entity.BusinessCreateRequest
import com.bookk.business.domain.api.business.entity.BusinessUpdateModel
import com.bookk.business.domain.api.business.operation.CreateBusiness
import com.bookk.business.domain.api.business.operation.GetBusinessById
import com.bookk.business.domain.api.business.operation.GetUserBusinesses
import com.bookk.business.domain.api.business.operation.UpdateBusiness
import com.bookk.business.microservice.route.BusinessRouting.Api
import com.bookk.core.service.enity.respondWith
import com.bookk.server.auth.client.AppPrincipal
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import org.koin.ktor.ext.inject

fun Route.businessCrud() {
    authenticate {
        /**
         * Summary: Create business
         * Description: Create new business with specific name
         * Tag: business
         * Security: jwt
         * Body: application/x-protobuf [com.bookk.business.domain.api.business.entity.BusinessCreateRequest]
         * Response: 422 application/x-protobuf [com.bookk.core.domain.entity.SimpleServerError] Create business errors<br>BUSINESS_ALREADY_EXIST (200001) Business already exist<br>BUSINESS_NAME_VALIDATION_ERROR (200002) Business name invalid
         */
         post<Api.Business> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val body = call.receive<BusinessCreateRequest>()
            val createBusiness by application.inject<CreateBusiness>()

            call.respondWith(createBusiness(userId = principal.userId, request = body))
         }
        /**
         * Summary: Update business
         * Description: Partially update business
         * Tag: business
         * Security: jwt
         * Body: application/x-protobuf [com.bookk.business.domain.api.business.entity.BusinessUpdateModel] Non-null fields will be updated
         * Response: 204 application/x-protobuf No content
         * Response: 400 application/x-protobuf Path id does not match body id, or the working schedule does not cover all 7 days
         * Response: 404 application/x-protobuf [com.bookk.core.domain.entity.SimpleServerError] User is not allowed to update the business
         * Response: 422 application/x-protobuf [com.bookk.core.domain.entity.SimpleServerError] Update business errors<br>BUSINESS_ACTIVE_DAY_WITHOUT_WORK_HOURS (200019) Active day must have at least one work hour<br>BUSINESS_INVALID_DAY_OFF_RANGE (200020) Day off range start date must be before end date
         */
        put<Api.Business.Id> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val body = call.receive<BusinessUpdateModel>()
            val updateBusiness by application.inject<UpdateBusiness>()

            if (it.id != body.id) {
                call.respond(HttpStatusCode.BadRequest, "Invalid request")
            } else {
                call.respondWith(updateBusiness(requestUserId = principal.userId, businessUpdateModel = body))
            }
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
