package com.bookk.business.data.datasource

import com.bookk.business.data.orm.entity.ServiceEntity
import com.bookk.business.data.orm.entity.ServiceGroupEntity
import com.bookk.business.data.orm.table.ServiceGroupTable
import com.bookk.business.data.orm.table.ServiceTable
import com.bookk.business.domain.api.service.entity.Service
import com.bookk.business.domain.api.service.entity.ServiceGroup
import com.bookk.business.domain.datasource.ServiceDataSource
import com.bookk.core.data.DataSource
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.time.Clock
import kotlin.uuid.Uuid

internal class ServiceDataSourceImpl : DataSource(), ServiceDataSource {
    override suspend fun createService(service: Service): Service = dbQuery {
        val id = ServiceTable.insertAndGetId {
            it[businessId] = service.businessId
            it[groupId] = service.group.id
            it[name] = service.name
            it[duration] = service.duration.inWholeSeconds.toInt()
            it[priceCurrency] = service.price.currencyUnit.code
            it[priceUnscaled] = service.price.amount.unscaledValue().longValueExact()
            it[priceScale] = service.price.scale
            it[available] = service.isAvailable
        }
        service.copy(id = id.value)
    }

    override suspend fun editService(service: Service): Service = dbQuery {
        ServiceTable.update(
            where = { ServiceTable.id eq service.id }
        ) {
            it[businessId] = service.businessId
            it[groupId] = service.group.id
            it[duration] = service.duration.inWholeSeconds.toInt()
            it[priceCurrency] = service.price.currencyUnit.code
            it[priceUnscaled] = service.price.amount.unscaledValue().longValueExact()
            it[priceScale] = service.price.scale
            it[available] = service.isAvailable
            it[updatedAt] = Clock.System.now()
        }
        service
    }

    override suspend fun getServices(businessId: Uuid): List<Service> = dbQuery {
        ServiceEntity.find {
            ServiceTable.businessId eq businessId
        }.map { it.toDomain() }
    }

    override suspend fun getServicesByIds(ids: List<Uuid>): List<Service> = dbQuery {
        ServiceEntity.find {
            ServiceTable.id inList ids.map { it }
        }.map { it.toDomain() }
    }

    override suspend fun deleteService(id: Uuid) {
        dbQuery { ServiceTable.deleteWhere { ServiceTable.id eq id } }
    }

    override suspend fun createServiceGroup(group: ServiceGroup): ServiceGroup = dbQuery {
        val id = ServiceGroupTable.insertAndGetId {
            it[businessId] = group.businessId
            it[name] = group.name
        }
        group.copy(id = id.value)
    }

    override suspend fun deleteServiceGroup(id: Uuid) {
        dbQuery { ServiceGroupTable.deleteWhere { ServiceGroupTable.id eq id } }
    }

    override suspend fun getServiceGroups(businessId: Uuid): List<ServiceGroup> = dbQuery {
        ServiceGroupEntity.find {
            ServiceGroupTable.businessId eq businessId
        }.map { it.toDomain() }
    }
}