package com.bookk.server.business.client.impl

import io.ktor.resources.Resource
import kotlin.uuid.Uuid

object BusinessRouting {
    @Resource("api")
    class Api {
        @Resource("/internal")
        class Internal(val parent: Api = Api()) {
            @Resource("/business")
            class Business(val parent: Internal = Internal()) {
                @Resource("/{id}")
                class Id(val parent: Business = Business(), val id: Uuid) {
                    @Resource("/permissions/{userId}")
                    class Permissions(val parent: Id, val userId: Uuid)
                }
            }
        }
    }
}
