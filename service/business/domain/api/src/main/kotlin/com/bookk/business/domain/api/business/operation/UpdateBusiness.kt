package com.bookk.business.domain.api.business.operation

import com.bookk.business.domain.api.business.entity.BusinessUpdateModel

interface UpdateBusiness {
    suspend operator fun invoke(businessUpdateModel: BusinessUpdateModel): Result<Unit>
}