package com.bookk.business.domain.api.service.operation

import com.bookk.business.domain.api.service.entity.Service

interface UpdateService {
    suspend operator fun invoke(service: Service): Result<Service>
}