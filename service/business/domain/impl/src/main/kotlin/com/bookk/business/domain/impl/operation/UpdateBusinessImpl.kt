package com.bookk.business.domain.impl.operation

import com.bookk.business.domain.api.entity.Business
import com.bookk.business.domain.api.entity.BusinessUpdateModel
import com.bookk.business.domain.api.operation.UpdateBusiness
import com.bookk.business.domain.datasource.BusinessDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager

internal class UpdateBusinessImpl(
    private val businessDataSource: BusinessDataSource,
    private val transactionManager: TransactionManager
) : UpdateBusiness {
    override suspend fun invoke(businessUpdateModel: BusinessUpdateModel): Result<Unit> = transactionManager.transaction {
        val name = businessUpdateModel.name?.take(Business.MAX_NAME_LENGTH)
        val description = businessUpdateModel.description?.take(Business.MAX_DESCRIPTION_LENGTH)
        val currencyCode = businessUpdateModel.currencyCode?.take(Business.MAX_CURRENCY_CODE)
        val address = businessUpdateModel.address?.take(Business.MAX_ADDRESS_LENGTH)
        val socials = businessUpdateModel.socials?.map {
            it.copy(value = it.value?.take(Business.MAX_SOCIAL_LENGTH))
        }
        businessDataSource.updateBusiness(
            businessUpdateModel.copy(
                name = name,
                description = description,
                currencyCode = currencyCode,
                address = address,
                socials = socials
            )
        )
    }
}