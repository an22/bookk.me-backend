package com.bookk.business.domain.api.business.entity

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import library.permissions.ResourcePermission

@Serializable
data class BusinessPermissions(
    @ProtoNumber(1) val business: ResourcePermission,
    @ProtoNumber(2) val employees: ResourcePermission,
    @ProtoNumber(3) val clients: ResourcePermission,
    @ProtoNumber(4) val services: ResourcePermission,
    @ProtoNumber(5) val appointments: ResourcePermission
) {
    operator fun get(resource: BusinessResource): ResourcePermission = when (resource) {
        BusinessResource.BUSINESS -> business
        BusinessResource.EMPLOYEES -> employees
        BusinessResource.CLIENTS -> clients
        BusinessResource.SERVICES -> services
        BusinessResource.APPOINTMENTS -> appointments
    }

    companion object {
        val NONE = BusinessPermissions(
            business = ResourcePermission.NONE,
            employees = ResourcePermission.NONE,
            clients = ResourcePermission.NONE,
            services = ResourcePermission.NONE,
            appointments = ResourcePermission.NONE
        )

        val FULL = BusinessPermissions(
            business = ResourcePermission.FULL,
            employees = ResourcePermission.FULL,
            clients = ResourcePermission.FULL,
            services = ResourcePermission.FULL,
            appointments = ResourcePermission.FULL
        )

        fun stub(
            business: ResourcePermission = ResourcePermission.NONE,
            employees: ResourcePermission = ResourcePermission.NONE,
            clients: ResourcePermission = ResourcePermission.NONE,
            services: ResourcePermission = ResourcePermission.NONE,
            appointments: ResourcePermission = ResourcePermission.NONE
        ) = BusinessPermissions(
            business = business,
            employees = employees,
            clients = clients,
            services = services,
            appointments = appointments
        )
    }
}
