package com.bookk.auth.microservice.route

import com.bookk.auth.microservice.route.api.deleteAccount
import com.bookk.auth.microservice.route.api.healthCheck
import com.bookk.auth.microservice.route.api.jwks
import com.bookk.auth.microservice.route.api.logOut
import com.bookk.auth.microservice.route.api.passkeyOperations
import com.bookk.auth.microservice.route.api.postRefreshToken
import com.bookk.auth.microservice.route.api.registration
import com.bookk.auth.microservice.route.api.signIn
import io.ktor.server.routing.Routing

fun Routing.authRoute() {
    healthCheck()
    jwks()
    passkeyOperations()
    signIn()
    registration()
    postRefreshToken()
    logOut()
    deleteAccount()
}