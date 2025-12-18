package com.bookk.user.data.datasource

import com.bookk.core.data.DataSource
import com.bookk.user.data.orm.table.ContactFormTable
import com.bookk.user.domain.api.entity.ContactForm
import com.bookk.user.domain.datasource.CommunicationDataSource
import org.jetbrains.exposed.v1.r2dbc.insert
import kotlin.time.Clock
import kotlin.uuid.toJavaUuid

internal class CommunicationDataSourceImpl : DataSource(), CommunicationDataSource {
    override suspend fun saveContactForm(form: ContactForm) {
        mapExceptions {
            dbQuery {
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