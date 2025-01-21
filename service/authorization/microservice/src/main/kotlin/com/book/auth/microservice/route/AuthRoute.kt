package com.book.auth.microservice.route

import com.book.auth.microservice.route.api.deleteAccount
import com.book.auth.microservice.route.api.deleteLogOut
import com.book.auth.microservice.route.api.getSignInChallenge
import com.book.auth.microservice.route.api.healthCheck
import com.book.auth.microservice.route.api.postFinishSignIn
import com.book.auth.microservice.route.api.postRefreshToken
import com.book.auth.microservice.route.api.postStartRegistration
import com.book.auth.microservice.route.api.postValidateRegistration
import io.ktor.server.routing.Routing

fun Routing.authRoute() {
    healthCheck()
    getSignInChallenge()
    postFinishSignIn()
    postStartRegistration()
    postValidateRegistration()
    postRefreshToken()
    deleteLogOut()
    deleteAccount()
}