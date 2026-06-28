package com.bookk.notifications.microservice.route

import io.ktor.resources.Resource

object NotificationsRouting {
    @Resource("api")
    class Api {

        @Resource("/notifications")
        class Notification(val parent: Api = Api()) {

            @Resource("/healthcheck")
            class HealthCheck(val parent: Notification = Notification())
        }
    }
}
