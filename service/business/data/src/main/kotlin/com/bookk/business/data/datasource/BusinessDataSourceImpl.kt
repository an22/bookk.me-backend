package com.bookk.business.data.datasource

import com.bookk.business.data.map.toDomain
import com.bookk.business.data.orm.entity.BusinessEntity
import com.bookk.business.data.orm.table.BusinessDashboardTable
import com.bookk.business.data.orm.table.BusinessTable
import com.bookk.business.domain.api.business.entity.Business
import com.bookk.business.domain.api.business.entity.BusinessUpdateModel
import com.bookk.business.domain.api.business.entity.UserBusinesses
import com.bookk.business.domain.datasource.BusinessDataSource
import com.bookk.core.data.DataSource
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.innerJoin
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

internal class BusinessDataSourceImpl : DataSource(), BusinessDataSource {
    override suspend fun createBusiness(userId: Uuid, name: String, currencyCode: String): Business = dbQuery {
        val javaUserId = userId.toJavaUuid()
        val id = BusinessTable.insertAndGetId {
            it[this.name] = name
            it[this.userId] = javaUserId
            it[this.currency] = currencyCode
            it[this.description] = ""
            it[this.address] = ""
        }
        BusinessDashboardTable.insert {
            it[this.userId] = javaUserId
            it[businessId] = id
        }
        BusinessTable.selectAll()
            .where { BusinessTable.id eq id.value }
            .map { BusinessEntity.wrapRow(it).toDomain() }
            .first()
    }

    override suspend fun updateBusiness(model: BusinessUpdateModel) = dbQuery<Unit> {
        BusinessTable.update(
            where = { BusinessTable.id eq model.id.toJavaUuid() }
        ) { statement ->
            model.name?.let { statement[name] = it }
            model.description?.let { statement[description] = it }
            model.address?.let { statement[address] = it }
            model.location?.let {
                statement[latitude] = it.lat
                statement[longitude] = it.lng
            }
            model.currencyCode?.let { statement[currency] = it }
            model.socials?.let {
                for (social in it) {
                    when (social.kind) {
                        Business.SocialKind.INSTAGRAM -> statement[instagram] = social.value
                        Business.SocialKind.TELEGRAM -> statement[telegram] = social.value
                        Business.SocialKind.VIBER -> statement[viber] = social.value
                        Business.SocialKind.WHATSAPP -> statement[whatsapp] = social.value
                        Business.SocialKind.PHONE -> statement[phone] = social.value
                    }
                }
            }
        }
    }

    override suspend fun getBusinessById(id: Uuid): Business? = dbQuery {
        BusinessTable.selectAll()
            .where { BusinessTable.id eq id.toJavaUuid() }
            .map { BusinessEntity.wrapRow(it).toDomain() }
            .singleOrNull()
    }

    override suspend fun isBusinessExist(userId: Uuid): Boolean = dbQuery {
        BusinessTable.select(BusinessTable.id)
            .where { BusinessTable.userId eq userId.toJavaUuid() }
            .empty()
            .not()
    }

    override suspend fun deleteUserBusinesses(userId: Uuid) = dbQuery<Unit> {
        BusinessTable.deleteWhere {
            BusinessTable.userId eq userId.toJavaUuid()
        }
    }

    override suspend fun getDashboardBusiness(userId: Uuid): Business? = dbQuery {
        BusinessDashboardTable
            .innerJoin(
                BusinessTable,
                onColumn = { businessId },
                otherColumn = { id }
            )
            .select(BusinessTable.columns)
            .where { BusinessDashboardTable.userId eq userId.toJavaUuid() }
            .map { BusinessEntity.wrapRow(it).toDomain() }
            .singleOrNull()
    }

    override suspend fun getUserBusinesses(userId: Uuid): UserBusinesses = dbQuery {
        val dashboardId = BusinessDashboardTable
            .select(BusinessDashboardTable.businessId)
            .where { BusinessDashboardTable.userId eq userId.toJavaUuid() }
            .singleOrNull()
            ?.getOrNull(BusinessDashboardTable.businessId)
            ?.value
        val businesses = BusinessTable
            .selectAll()
            .where { BusinessTable.userId eq userId.toJavaUuid() }
            .map { BusinessEntity.wrapRow(it).toDomain() }
            .toList()
        UserBusinesses(
            dashboardId = dashboardId?.toKotlinUuid(),
            businesses = businesses
        )
    }
}