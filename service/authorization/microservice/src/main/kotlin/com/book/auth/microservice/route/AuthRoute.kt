package com.book.auth.microservice.route

import com.book.auth.microservice.route.api.deleteAccount
import com.book.auth.microservice.route.api.deleteLogOut
import com.book.auth.microservice.route.api.getSignUpChallenge
import com.book.auth.microservice.route.api.healthCheck
import com.book.auth.microservice.route.api.postCreateAccount
import com.book.auth.microservice.route.api.postLogin
import com.book.auth.microservice.route.api.postRefreshToken
import com.book.auth.microservice.route.api.postValidateRegistration
import io.ktor.server.routing.Routing

fun Routing.authRoute() {
    healthCheck()
    postLogin()
    postCreateAccount()
    getSignUpChallenge()
    postValidateRegistration()
    postRefreshToken()
    deleteLogOut()
    deleteAccount()
}