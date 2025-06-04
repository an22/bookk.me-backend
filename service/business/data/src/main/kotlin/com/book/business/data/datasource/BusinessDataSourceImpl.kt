package com.book.business.data.datasource

import com.book.business.data.map.toDomain
import com.book.business.data.orm.entity.BusinessEntity
import com.book.business.data.orm.entity.DashboardBusinessEntity
import com.book.business.data.orm.table.BusinessDashboardTable
import com.book.business.data.orm.table.BusinessTable
import com.book.business.domain.api.entity.Business
import com.book.business.domain.api.entity.BusinessUpdateModel
import com.book.business.domain.datasource.BusinessDataSource
import com.book.core.data.DataSource
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

internal class BusinessDataSourceImpl : DataSource(), BusinessDataSource {
    override suspend fun createBusiness(userId: Long, name: String): Business {
        return mapExceptions {
            transaction {
                val business = BusinessEntity.new {
                    this.name = name
                    this.userId = userId
                }.toDomain()
                BusinessDashboardTable.insert {
                    it[this.userId] = userId
                    it[businessId] = business.id
                }
                business
            }
        }
    }

    override suspend fun updateBusiness(model: BusinessUpdateModel) {
        return mapExceptions {
            transaction {
                BusinessTable.update(
                    where = { BusinessTable.id eq model.id }
                ) { statement ->
                    model.name?.let { statement[name] = it }
                    model.description?.let { statement[description] = it }
                    model.location?.let {
                        statement[latitude] = it.lat
                        statement[longitude] = it.lng
                    }
                    model.currencyUnit?.let { statement[currency] = it.code }
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

    override suspend fun getBusinessById(id: Long): Business? {
        return mapExceptions {
            transaction {
                BusinessEntity.findById(id)?.toDomain()
            }
        }
    }

    override suspend fun isBusinessExist(userId: Long): Boolean {
        return mapExceptions {
            transaction {
                BusinessTable.select(BusinessTable.id)
                    .where { BusinessTable.userId eq userId }
                    .empty()
                    .not()
            }
        }
    }

    override suspend fun deleteUserBusinesses(userId: Long) {
        return mapExceptions {
            transaction {
                BusinessTable.deleteWhere {
                    BusinessTable.userId eq userId
                }
            }
        }
    }

    override suspend fun getDashboardBusiness(userId: Long): Business? {
        return mapExceptions {
            transaction {
                DashboardBusinessEntity.find {
                    BusinessDashboardTable.userId eq userId
                }
                    .firstOrNull()
                    ?.business
                    ?.toDomain()
            }
        }
    }
}