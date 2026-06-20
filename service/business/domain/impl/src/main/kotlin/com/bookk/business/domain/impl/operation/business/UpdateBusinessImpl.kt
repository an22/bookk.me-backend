package com.bookk.business.domain.impl.operation.business

import com.bookk.business.domain.api.business.entity.Business
import com.bookk.business.domain.api.business.entity.BusinessUpdateModel
import com.bookk.business.domain.api.business.operation.UpdateBusiness
import com.bookk.business.domain.datasource.BusinessDataSource
import com.bookk.core.data.eventstreaming.StandardEventProducer
import com.bookk.core.data.eventstreaming.send
import com.bookk.core.domain.datasource.transaction.TransactionManager
import com.bookk.server.business.client.api.event.BusinessEvent

internal class UpdateBusinessImpl(
    private val businessDataSource: BusinessDataSource,
    private val transactionManager: TransactionManager,
    private val eventProducer: StandardEventProducer,
) : UpdateBusiness {
    override suspend fun invoke(businessUpdateModel: BusinessUpdateModel): Result<Unit> =
        transactionManager.transaction {
            val name = businessUpdateModel.name?.take(Business.MAX_NAME_LENGTH)
            val description = businessUpdateModel.description?.take(Business.MAX_DESCRIPTION_LENGTH)
            val currencyCode = businessUpdateModel.currencyCode?.take(Business.MAX_CURRENCY_CODE)
            val address = businessUpdateModel.address?.take(Business.MAX_ADDRESS_LENGTH)
            val socials = businessUpdateModel.socials?.map {
                it.copy(value = it.value?.take(Business.MAX_SOCIAL_LENGTH))
            }
            val updatedModel = businessUpdateModel.copy(
                name = name,
                description = description,
                currencyCode = currencyCode,
                address = address,
                socials = socials
            )
            businessDataSource.updateBusiness(updatedModel).also { business ->
                eventProducer.send(
                    BusinessEvent.Updated(
                        BusinessEvent.BusinessDTO(
                            id = business.id,
                            name = business.name,
                            address = business.address,
                            timeZone = business.timeZone
                        )
                    )
                )
            }
        }
}