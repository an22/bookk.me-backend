package com.book.user.data.datasource

import com.book.core.data.DataSource
import com.book.user.data.orm.table.ContactFormTable
import com.book.user.domain.api.entity.ContactForm
import com.book.user.domain.datasource.CommunicationDataSource
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

internal class CommunicationDataSourceImpl : DataSource(), CommunicationDataSource {
    override suspend fun saveContactForm(form: ContactForm) {
        mapExceptions {
            transaction {
                ContactFormTable.insert {
                    it[userId] = form.userId
                    it[text] = form.text
                    it[usageLogs] = form.usageLogs
                    it[updatedAt] = Clock.System.now()
                    it[status] = form.status.id
                }
            }
        }
    }
}