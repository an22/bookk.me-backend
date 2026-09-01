package com.bookk.business.domain.impl.operation.client

import com.bookk.business.domain.api.client.entity.Client
import com.bookk.business.domain.api.client.entity.ClientRemote
import com.bookk.business.domain.api.client.entity.ClientUpdateModel
import com.bookk.business.domain.api.client.entity.toRemote
import com.bookk.business.domain.api.client.operation.UpdateClient
import com.bookk.business.domain.datasource.BusinessDataSource
import com.bookk.business.domain.datasource.ClientDataSource
import com.bookk.core.domain.datasource.transaction.TransactionManager
import library.permissions.ObjectPermission
import library.permissions.assert
import library.validation.EmailValidator
import library.validation.NameValidator
import library.validation.PhoneValidator
import kotlin.uuid.Uuid

internal class UpdateClientImpl(
    private val transactionManager: TransactionManager,
    private val clientDataSource: ClientDataSource,
    private val businessDataSource: BusinessDataSource
) : UpdateClient {
    override suspend fun invoke(requestUserId: Uuid, businessId: Uuid, model: ClientUpdateModel): Result<ClientRemote> =
        transactionManager.transaction {
            businessDataSource.getPermission(requestUserId, businessId).assert(ObjectPermission.EDIT)
            val existing = clientDataSource.getClientById(businessId, model.id) ?: throw UpdateClient.Error.NotFound()

            val personalInfoRequested = model.name != null || model.lastName != null || model.phone != null || model.email != null
            if (existing is Client.Integrated && personalInfoRequested) {
                throw UpdateClient.Error.PersonalInfoNotEditable()
            }

            model.name?.let { name ->
                if (!NameValidator.isValid(name, minLength = 0, maxLength = MAX_NAME_LENGTH)) {
                    throw UpdateClient.Error.ClientValidationError()
                }
            }
            model.lastName?.let { lastName ->
                if (!NameValidator.isValid(lastName, minLength = 0, maxLength = MAX_NAME_LENGTH)) {
                    throw UpdateClient.Error.ClientValidationError()
                }
            }
            model.phone?.let { phone ->
                if (!PhoneValidator.isValid(phone)) throw UpdateClient.Error.ClientValidationError()
                val conflict = clientDataSource.getClient(businessId, phone)
                if (conflict != null && conflict.id != model.id) throw UpdateClient.Error.ClientExist()
            }
            model.email?.let { email ->
                if (!EmailValidator.isValid(email)) throw UpdateClient.Error.ClientValidationError()
            }

            val trimmedModel = model.copy(
                name = model.name?.trim(),
                lastName = model.lastName?.trim(),
                phone = model.phone?.trim(),
                email = model.email?.trim(),
                description = model.description?.trim()?.take(Client.MAX_DESCRIPTION_LENGTH)
            )

            (clientDataSource.updateClient(businessId, trimmedModel) ?: throw UpdateClient.Error.NotFound()).toRemote()
        }

    companion object {
        const val MAX_NAME_LENGTH = 512
    }
}
