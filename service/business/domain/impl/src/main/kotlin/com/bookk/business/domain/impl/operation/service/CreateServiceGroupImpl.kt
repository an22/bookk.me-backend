package com.bookk.business.domain.impl.operation.service

import com.bookk.business.domain.api.service.entity.ServiceGroup
import com.bookk.business.domain.api.service.operation.CreateServiceGroup
import com.bookk.business.domain.datasource.BusinessDataSource
import com.bookk.business.domain.datasource.ServiceDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.core.domain.entity.onConstraintFailure
import library.permissions.ObjectPermission
import library.permissions.assert
import kotlin.uuid.Uuid

internal class CreateServiceGroupImpl(
    private val dataSource: ServiceDataSource,
    private val businessDataSource: BusinessDataSource,
    private val transactionManager: TransactionManager
) : CreateServiceGroup {
    override suspend fun invoke(requestUserId: Uuid, service: ServiceGroup): Result<ServiceGroup> {
        if (service.name.isBlank()) return Result.failure(CreateServiceGroup.Error.ValidationError())
        return transactionManager.transaction {
            businessDataSource.getPermission(requestUserId, service.businessId).assert(ObjectPermission.WRITE)
            dataSource.createServiceGroup(service)
        }.onConstraintFailure {
            throw CreateServiceGroup.Error.ServiceGroupExist()
        }
    }
}