package com.bookk.server.user.client.impl

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
            }
        }
    }
}