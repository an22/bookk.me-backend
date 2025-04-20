package com.book.business.data.datasource

import com.book.business.data.map.toDomain
import com.book.business.data.orm.entity.BusinessEntity
import com.book.business.data.orm.table.BusinessTable
import com.book.business.domain.api.entity.Business
import com.book.business.domain.datasource.BusinessDataSource
import com.book.core.data.DataSource
import org.jetbrains.exposed.sql.transactions.transaction

internal class BusinessDataSourceImpl : DataSource(), BusinessDataSource {
    override suspend fun createBusiness(name: String): Business {
        return mapExceptions {
            transaction {
                BusinessEntity.new {
                    this.name = name
                }.toDomain()
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
}