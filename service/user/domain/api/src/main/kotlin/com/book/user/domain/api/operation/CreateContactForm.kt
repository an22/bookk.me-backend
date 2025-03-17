package com.book.user.domain.api.operation

import com.book.user.domain.api.entity.ContactForm

interface CreateContactForm {
    suspend operator fun invoke(form: ContactForm): Result<Unit>
}