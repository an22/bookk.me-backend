package com.bookk.auth.microservice.route.api

import com.bookk.auth.domain.api.delete_account.entity.VerifyDeleteAccRequest
import com.bookk.auth.domain.api.delete_account.operation.DeleteAccount
import com.bookk.auth.microservice.route.AuthRouting.Api
import com.bookk.core.service.auth.AppPrincipal
import com.bookk.core.service.enity.respondWith
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.resources.delete
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import org.koin.ktor.ext.inject

internal fun Route.deleteAccount() {
    authenticate {
        /**
         * Delete Account
         * @description Delete user account and all associated data attached to current auth token
         * @tag *auth
         * @security jwt
         * @response 200 Success
         */
        delete<Api.Auth.Account> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val body = call.receive<VerifyDeleteAccRequest>()
            val deleteAccount by application.inject<DeleteAccount>()

            call.respondWith(deleteAccount(principal.userId, body))
        }
    }
}