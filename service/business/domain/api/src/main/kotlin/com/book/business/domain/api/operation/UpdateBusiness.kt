package com.book.business.domain.api.operation

import com.book.business.domain.api.entity.BusinessUpdateModel

interface UpdateBusiness {
    suspend operator fun invoke(businessUpdateModel: BusinessUpdateModel): Result<Unit>
}