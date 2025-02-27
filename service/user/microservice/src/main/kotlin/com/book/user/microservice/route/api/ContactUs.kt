package com.book.user.microservice.route.api

import com.book.core.service.applyMediaType
import com.book.core.service.auth.AppPrincipal
import com.book.core.service.enity.respondWith
import com.book.user.domain.api.entity.ContactForm
import com.book.user.domain.api.operation.CreateContactForm
import com.book.user.microservice.route.UserRouting.Api
import io.bkbn.kompendium.core.metadata.PostInfo
import io.bkbn.kompendium.resources.NotarizedResource
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.resources.post
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject

@Serializable
private class ContactFormBody(
    val text: String,
    val usageLogs: String?
) {
    fun asForm(userId: Long): ContactForm {
        return ContactForm(
            userId = userId,
            text = text,
            usageLogs = usageLogs
        )
    }
}

internal fun Route.postContactForm() {
    withContactUsDocumentation()
    authenticate {
        post<Api.User.ContactUs> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val body = requireNotNull(call.receive<ContactFormBody>())
            val createForm by application.inject<CreateContactForm>()

            call.respondWith(createForm(body.asForm(principal.userId)))
        }
    }
}

internal fun Route.withContactUsDocumentation() {
    install(NotarizedResource<Api.User.ContactUs>()) {
        tags = setOf("user")
        post = PostInfo.builder {
            security = mapOf("jwt" to emptyList())
            summary("Send contact form")
            description("Create contact/support form")
            request {
                applyMediaType()
                requestType<ContactFormBody>()
                description("Contact form created by client")
            }
            response {
                applyMediaType()
                responseCode(HttpStatusCode.Created)
                responseType<Unit>()
                description("User associated with provided credentials")
            }
        }
    }
}