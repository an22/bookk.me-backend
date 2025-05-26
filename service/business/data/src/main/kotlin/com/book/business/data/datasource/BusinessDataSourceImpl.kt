package com.book.business.data.datasource

import com.book.business.data.map.toDomain
import com.book.business.data.orm.entity.BusinessEntity
import com.book.business.data.orm.entity.DashboardBusinessEntity
import com.book.business.data.orm.table.BusinessDashboardTable
import com.book.business.data.orm.table.BusinessTable
import com.book.business.domain.api.entity.Business
import com.book.business.domain.datasource.BusinessDataSource
import com.book.core.data.DataSource
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

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