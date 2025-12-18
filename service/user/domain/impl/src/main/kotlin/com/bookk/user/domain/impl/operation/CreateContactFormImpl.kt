package com.bookk.user.domain.impl.operation

import com.bookk.user.domain.api.entity.ContactForm
import com.bookk.user.domain.api.operation.CreateContactForm
import com.bookk.user.domain.datasource.CommunicationDataSource

internal class CreateContactFormImpl(
    private val communicationDataSource: CommunicationDataSource
) : CreateContactForm {
    override suspend fun invoke(form: ContactForm): Result<Unit> = runCatching {
        val adjustedForm = if (form.isBoundCapRequired) {
            form.copy(
                text = form.text.take(ContactForm.TEXT_UPPER_BOUND.toInt()),
                usageLogs = form.usageLogs?.take(ContactForm.LOGS_UPPER_BOUND.toInt())
            )
        } else {
            form
        }
        communicationDataSource.saveContactForm(adjustedForm)
    }
}