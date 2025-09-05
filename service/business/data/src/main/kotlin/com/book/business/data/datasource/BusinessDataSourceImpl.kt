package com.book.business.data.datasource

import com.book.business.data.map.toDomain
import com.book.business.data.orm.entity.BusinessEntity
import com.book.business.data.orm.table.BusinessDashboardTable
import com.book.business.data.orm.table.BusinessTable
import com.book.business.domain.api.entity.Business
import com.book.business.domain.api.entity.BusinessUpdateModel
import com.book.business.domain.api.entity.UserBusinesses
import com.book.business.domain.datasource.BusinessDataSource
import com.book.core.data.DataSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.innerJoin
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.insertAndGetId
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.update
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

internal class BusinessDataSourceImpl : DataSource(), BusinessDataSource {
    override suspend fun createBusiness(userId: Uuid, name: String, currencyCode: String): Business {
        val javaUserId = userId.toJavaUuid()
        return mapExceptions {
            dbQuery {
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
                    .map { BusinessEntity.wrapRowR2dbc(it).toDomain() }
                    .first()
            }
        }
    }

    override suspend fun updateBusiness(model: BusinessUpdateModel) {
        return mapExceptions {
            dbQuery {
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
        }
    }

    override suspend fun getBusinessById(id: Uuid): Business? {
        return mapExceptions {
            dbQuery {
                BusinessTable.selectAll()
                    .where { BusinessTable.id eq id.toJavaUuid() }
                    .map { BusinessEntity.wrapRowR2dbc(it).toDomain() }
                    .singleOrNull()
            }
        }
    }

    override suspend fun isBusinessExist(userId: Uuid): Boolean {
        return mapExceptions {
            dbQuery {
                BusinessTable.select(BusinessTable.id)
                    .where { BusinessTable.userId eq userId.toJavaUuid() }
                    .empty()
                    .not()
            }
        }
    }

    override suspend fun deleteUserBusinesses(userId: Uuid) {
        return mapExceptions {
            dbQuery {
                BusinessTable.deleteWhere {
                    BusinessTable.userId eq userId.toJavaUuid()
                }
            }
        }
    }

    override suspend fun getDashboardBusiness(userId: Uuid): Business? {
        return mapExceptions {
            dbQuery {
                BusinessDashboardTable
                    .innerJoin(
                        BusinessTable,
                        onColumn = { businessId },
                        otherColumn = { id }
                    )
                    .select(BusinessTable.columns)
                    .where { BusinessDashboardTable.userId eq userId.toJavaUuid() }
                    .map { BusinessEntity.wrapRowR2dbc(it).toDomain() }
                    .singleOrNull()
            }
        }
    }

    override suspend fun getUserBusinesses(userId: Uuid): UserBusinesses {
        return mapExceptions {
            dbQuery {
                val dashboardId = BusinessDashboardTable
                    .select(BusinessDashboardTable.businessId)
                    .where { BusinessDashboardTable.userId eq userId.toJavaUuid() }
                    .singleOrNull()
                    ?.getOrNull(BusinessDashboardTable.businessId)
                    ?.value
                val businesses = BusinessTable
                    .selectAll()
                    .where { BusinessTable.userId eq userId.toJavaUuid() }
                    .map { BusinessEntity.wrapRowR2dbc(it).toDomain() }
                    .toList()
                UserBusinesses(
                    dashboardId = dashboardId?.toKotlinUuid(),
                    businesses = businesses
                )
            }
        }
    }
}