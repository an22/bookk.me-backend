package com.bookk.user.microservice.route.api

import com.bookk.core.service.auth.AppPrincipal
import com.bookk.core.service.enity.respondWith
import com.bookk.user.domain.api.entity.ContactForm
import com.bookk.user.domain.api.operation.CreateContactForm
import com.bookk.user.microservice.route.UserRouting.Api
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.resources.post
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import kotlin.uuid.Uuid

@Serializable
internal class ContactFormBody(
    val text: String,
    val usageLogs: String?
) {
    fun asForm(userId: Uuid): ContactForm {
        return ContactForm(
            userId = userId,
            text = text,
            usageLogs = usageLogs,
            status = ContactForm.ContactFormStatus.NEW
        )
    }
}

internal fun Route.postContactForm() {
    authenticate {
        /**
         * Summary: Send contact form
         * Description: Create contact/support form
         * Tag: user
         * Security: jwt
         * RequestBody: application/x-protobuf [com.bookk.user.microservice.route.api.ContactFormBody] Contact form created by client
         * Response: 201 application/x-protobuf Contact form submitted
         */
        post<Api.User.ContactUs> {
            val principal = requireNotNull(call.principal<AppPrincipal>())
            val body = requireNotNull(call.receive<ContactFormBody>())
            val createForm by application.inject<CreateContactForm>()

            call.respondWith(createForm(body.asForm(principal.userId)))
        }
    }
}
