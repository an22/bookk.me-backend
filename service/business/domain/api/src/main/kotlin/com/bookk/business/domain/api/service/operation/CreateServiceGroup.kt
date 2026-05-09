package com.bookk.business.domain.api.service.operation

import com.bookk.business.domain.api.service.entity.ServiceGroup

interface CreateServiceGroup {
    suspend operator fun invoke(service: ServiceGroup): Result<ServiceGroup>
}