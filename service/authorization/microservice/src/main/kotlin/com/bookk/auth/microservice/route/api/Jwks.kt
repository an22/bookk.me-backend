package com.bookk.auth.microservice.route.api

import com.bookk.auth.domain.api.token.operation.GetVerificationKeys
import com.bookk.core.service.auth.JwkSet
import com.bookk.core.service.auth.createJwk
import com.bookk.core.service.auth.jwksJson
import io.ktor.http.ContentType
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.routing.get
import org.koin.ktor.ext.inject


internal fun Route.jwks() {
    /**
     * Summary: JSON Web Key Set
     * Description: Publishes the public keys used to verify access tokens, keyed by kid.
     * Tag: auth
     */
    get("/jwks.json") {
        val getVerificationKeys by application.inject<GetVerificationKeys>()
        val keys = getVerificationKeys().getOrThrow().map { createJwk(it.id, it.publicKeyPem) }
        call.respondText(jwksJson.encodeToString(JwkSet(keys)), ContentType.Application.Json)
    }
}
