package com.bookk.business.data.datasource

import com.bookk.business.data.orm.entity.BusinessEntity
import com.bookk.business.data.orm.table.BusinessDashboardTable
import com.bookk.business.data.orm.table.BusinessDayOffTable
import com.bookk.business.data.orm.table.BusinessPermissionsTable
import com.bookk.business.data.orm.table.BusinessTable
import com.bookk.business.domain.api.business.entity.Business
import com.bookk.business.domain.api.business.entity.BusinessUpdateModel
import com.bookk.business.domain.api.business.entity.UserBusinesses
import com.bookk.business.domain.datasource.BusinessDataSource
import com.bookk.core.data.DataSource
import com.bookk.core.domain.entity.Error
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.jdbc.deleteReturning
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.upsert
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

internal class BusinessDataSourceImpl : DataSource(), BusinessDataSource {
    override suspend fun createBusiness(userId: Uuid, name: String, currencyCode: String, timeZone: TimeZone): Business = dbQuery {
        val javaUserId = userId.toJavaUuid()
        val entity = BusinessEntity.new(javaUserId, name, currencyCode, timeZone)
        BusinessDashboardTable.insert {
            it[this.userId] = javaUserId
            it[businessId] = entity.id
        }
        entity.toDomain()
    }

    override suspend fun updateBusiness(model: BusinessUpdateModel, updatedAt: Instant) = dbQuery<Business> {
        val entity = BusinessEntity.findByIdAndUpdate(model, updatedAt) ?: throw Error.NotFound()
        entity.toDomain()
    }

    override suspend fun getBusinessById(id: Uuid): Business? = dbQuery {
        BusinessEntity.findById(id.toJavaUuid())?.toDomain()
    }

    override suspend fun isBusinessExist(userId: Uuid): Boolean = dbQuery {
        BusinessTable.select(BusinessTable.id)
            .where { BusinessTable.userId eq userId.toJavaUuid() }
            .empty()
            .not()
    }

    override suspend fun deleteUserBusinesses(userId: Uuid) = dbQuery {
        BusinessTable.deleteReturning(listOf(BusinessTable.id)) {
            BusinessTable.userId eq userId.toJavaUuid()
        }.map { it[BusinessTable.id].value.toKotlinUuid() }
    }

    override suspend fun getDashboardBusiness(userId: Uuid): Business? = dbQuery {
        val businessId = BusinessDashboardTable
            .select(BusinessDashboardTable.businessId)
            .where { BusinessDashboardTable.userId eq userId.toJavaUuid() }
            .singleOrNull()
            ?.get(BusinessDashboardTable.businessId)
            ?.value
        businessId?.let { BusinessEntity.findById(it)?.toDomain() }
    }

    override suspend fun getUserBusinesses(userId: Uuid): UserBusinesses = dbQuery {
        val dashboardId = BusinessDashboardTable
            .select(BusinessDashboardTable.businessId)
            .where { BusinessDashboardTable.userId eq userId.toJavaUuid() }
            .singleOrNull()
            ?.getOrNull(BusinessDashboardTable.businessId)
            ?.value
        val businesses = BusinessEntity
            .find { BusinessTable.userId eq userId.toJavaUuid() }
            .map { it.toDomain() }
        UserBusinesses(
            dashboardId = dashboardId?.toKotlinUuid(),
            businesses = businesses
        )
    }

    override suspend fun deleteDayOffsInThePast() = dbQuery {
        val now = Clock.System.now()
        val businessIdsByTimeZone = BusinessTable
            .select(BusinessTable.id, BusinessTable.timezone)
            .groupBy(
                keySelector = { it[BusinessTable.timezone] },
                valueTransform = { it[BusinessTable.id].value }
            )

        businessIdsByTimeZone.forEach { (timeZone, businessIds) ->
            val today = now.toLocalDateTime(TimeZone.of(timeZone)).date
            BusinessDayOffTable.deleteWhere {
                BusinessDayOffTable.businessId.inList(businessIds)
                    .and(BusinessDayOffTable.endDate.less(today))
            }
        }
    }

    override suspend fun getPermission(userId: Uuid, businessId: Uuid): Int? = dbQuery {
        BusinessPermissionsTable.select(
            BusinessPermissionsTable.permission
        )
            .where { (BusinessPermissionsTable.userId eq userId.toJavaUuid()) and (BusinessPermissionsTable.businessId eq businessId.toJavaUuid()) }
            .singleOrNull()
            ?.get(BusinessPermissionsTable.permission)
    }

    override suspend fun setUserPermissions(userId: Uuid, businessId: Uuid, permission: Int) {
        BusinessPermissionsTable.upsert {
            it[this.userId] = userId.toJavaUuid()
            it[this.businessId] = businessId.toJavaUuid()
            it[this.permission] = permission
        }
    }
}
