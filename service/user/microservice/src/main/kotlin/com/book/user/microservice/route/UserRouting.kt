package com.book.user.microservice.route

import io.ktor.resources.Resource

object UserRouting {
    @Resource("api")
    class Api {
        @Resource("/internal")
        class Internal(val parent: Api = Api()) {
            @Resource("/user")
            class User(val parent: Internal = Internal()) {
                @Resource("/{id}")
                class Id(val parent: User = User(), val id: Long)

                @Resource("/{id}")
                class Delete(val parent: User = User(), val id: Long)

                @Resource("/{id}")
                class Edit(val parent: User = User(), val id: Long)
            }
        }

        @Resource("/user")
        class User(val parent: Api = Api()) {

            @Resource("/healthcheck")
            class HealthCheck(val parent: User = User())

            @Resource("/me")
            class Me(val parent: User = User())

            @Resource("/me")
            class Edit(val parent: User = User())
        }
    }
}