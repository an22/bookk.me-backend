package com.book.user.domain.datasource

import com.book.user.domain.api.entity.ContactForm

interface CommunicationDataSource {
    suspend fun saveContactForm(form: ContactForm)
}