package com.book.auth.microservice.route.api

import com.book.auth.domain.api.delete_account.operation.DeleteAccount
import com.book.auth.microservice.route.AuthRouting.Api
import com.book.core.service.applyMediaType
import com.book.core.service.auth.AppPrincipal
import com.book.core.service.enity.respondWith
import io.bkbn.kompendium.core.metadata.DeleteInfo
import io.bkbn.kompendium.resources.NotarizedResource
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.resources.delete
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import org.koin.ktor.ext.inject

internal fun Route.deleteAccount() {
    withDeleteAccountDocumentation()
    authenticate {
        delete<Api.Auth.DeleteAccount> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val deleteAccount by application.inject<DeleteAccount>()

            call.respondWith(deleteAccount(principal.userId))
        }
    }
}

internal fun Route.withDeleteAccountDocumentation() {
    install(NotarizedResource<Api.Auth.DeleteAccount>()) {
        tags = setOf("auth")
        security = mapOf("jwt" to emptyList())
        delete = DeleteInfo.builder {
            summary("Delete Account")
            description("Delete user account and all associated data")
            response {
                applyMediaType()
                responseCode(HttpStatusCode.OK)
                responseType<Unit>()
                description("Success operation")
            }
        }
    }
}