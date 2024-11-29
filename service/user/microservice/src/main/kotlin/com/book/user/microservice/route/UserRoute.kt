package com.book.user.microservice.route

import com.book.user.microservice.route.api.deleteUser
import com.book.user.microservice.route.api.getCurrentUser
import com.book.user.microservice.route.api.getHealthCheck
import com.book.user.microservice.route.api.getUserById
import com.book.user.microservice.route.api.postCheckExistence
import com.book.user.microservice.route.api.postCreateUser
import io.ktor.server.routing.Routing


fun Routing.userRoute() {
    getHealthCheck()
    getUserById()
    postCreateUser()
    postCheckExistence()
    deleteUser()
    getCurrentUser()
}