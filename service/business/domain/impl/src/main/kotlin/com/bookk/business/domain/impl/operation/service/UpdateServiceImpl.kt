package com.bookk.business.domain.impl.operation.service

import com.bookk.business.domain.api.service.entity.Service
import com.bookk.business.domain.api.service.operation.CreateService
import com.bookk.business.domain.api.service.operation.UpdateService
import com.bookk.business.domain.datasource.BusinessDataSource
import com.bookk.business.domain.datasource.ServiceDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import library.permissions.ObjectPermission
import library.permissions.assert
import kotlin.uuid.Uuid

internal class UpdateServiceImpl(
    private val dataSource: ServiceDataSource,
    private val businessDataSource: BusinessDataSource,
    private val transactionManager: TransactionManager
): UpdateService {
    override suspend fun invoke(requestUserId: Uuid, service: Service): Result<Service> {
        if (service.name.isBlank()) return Result.failure(CreateService.Error.ValidationError())
        return transactionManager.transaction {
            businessDataSource.getPermission(requestUserId, service.businessId).assert(ObjectPermission.EDIT)
            dataSource.editService(service)
        }
    }
}