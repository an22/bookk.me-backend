package com.bookk.business.domain.api.operation

import com.bookk.business.domain.api.entity.BusinessUpdateModel

interface UpdateBusiness {
    suspend operator fun invoke(businessUpdateModel: BusinessUpdateModel): Result<Unit>
}