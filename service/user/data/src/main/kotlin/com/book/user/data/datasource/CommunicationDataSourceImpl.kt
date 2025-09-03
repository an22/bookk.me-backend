package com.book.user.data.datasource

import com.book.core.data.DataSource
import com.book.user.data.orm.table.ContactFormTable
import com.book.user.domain.api.entity.ContactForm
import com.book.user.domain.datasource.CommunicationDataSource
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import kotlin.time.Clock
import kotlin.uuid.toJavaUuid

internal class CommunicationDataSourceImpl : DataSource(), CommunicationDataSource {
    override suspend fun saveContactForm(form: ContactForm) {
        mapExceptions {
            suspendTransaction {
                ContactFormTable.insert {
                    it[userId] = form.userId.toJavaUuid()
                    it[text] = form.text
                    it[usageLogs] = form.usageLogs
                    it[updatedAt] = Clock.System.now()
                    it[status] = form.status.id
                }
            }
        }
    }
}