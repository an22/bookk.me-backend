package com.book.user.microservice.route

import com.book.user.microservice.route.api.getCurrentUser
import com.book.user.microservice.route.api.getHealthCheck
import com.book.user.microservice.route.api.internal.deleteUser
import com.book.user.microservice.route.api.internal.getUserByEmail
import com.book.user.microservice.route.api.internal.getUserById
import com.book.user.microservice.route.api.internal.postCreateUser
import com.book.user.microservice.route.api.patchUser
import io.ktor.server.routing.Routing


fun Routing.userRoute() {
    getHealthCheck()
    getUserById()
    getUserByEmail()
    postCreateUser()
    deleteUser()
    getCurrentUser()
    patchUser()
}