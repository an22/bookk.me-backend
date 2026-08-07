package com.bookk.business.microservice.route

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

        @Resource("/business")
        class Business(val parent: Api = Api()) {

            @Resource("/healthcheck")
            class HealthCheck(val parent: Business = Business())

            @Resource("/{id}")
            class Id(val parent: Business = Business(), val id: Uuid)
        }

        @Resource("/business/{businessId}/clients")
        class Clients(val parent: Api = Api(), val businessId: Uuid) {
            @Resource("/{id}")
            class Id(val parent: Clients, val id: Uuid)
        }

        @Resource("/business/{businessId}/service")
        class Service(val parent: Api = Api(), val businessId: Uuid) {
            @Resource("/{id}")
            class Id(val parent: Service, val id: Uuid)

            @Resource("/quote")
            class Quote(val parent: Service)
        }

        @Resource("/business/{businessId}/employee_invitation")
        class EmployeeInvitation(val parent: Api = Api(), val businessId: Uuid) {
            @Resource("/{id}/approve")
            class Approve(val parent: EmployeeInvitation, val id: Uuid)
        }

        @Resource("/business/{businessId}/employee")
        class Employee(val parent: Api = Api(), val businessId: Uuid) {
            @Resource("/{id}")
            class Id(val parent: Employee, val id: Uuid)
        }

        @Resource("/business/{businessId}/service_group")
        class ServiceGroup(val parent: Api = Api(), val businessId: Uuid) {
            @Resource("/{id}")
            class Id(val parent: ServiceGroup, val id: Uuid)
        }
    }
}