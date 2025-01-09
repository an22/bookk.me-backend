package com.book.auth.data.di

import com.book.auth.data.datasource.PassKeyDataSourceImpl
import com.book.auth.data.datasource.UserAuthDataSourceImpl
import com.book.auth.data.datasource.YubicoCredentialRepository
import com.book.auth.data.orm.table.AuthDeviceTable
import com.book.auth.data.orm.table.AuthenticationTable
import com.book.auth.data.orm.table.PasskeyCredentialTable
import com.book.auth.domain.api.datasource.PassKeyDataSource
import com.book.auth.domain.api.datasource.UserAuthDataSource
import com.book.core.data.database.createDatabase
import com.bookk.server.user.client.di.userClientModule
import com.yubico.webauthn.CredentialRepository
import org.jetbrains.exposed.sql.Table
import org.koin.dsl.module

fun authDataModule() = module {
    includes(userClientModule(System.getenv("BOOKK_ME_SERVICE_NAME")))
    single<UserAuthDataSource> { UserAuthDataSourceImpl(get()) }
    single<PassKeyDataSource> { PassKeyDataSourceImpl(get()) }
    single<CredentialRepository> { YubicoCredentialRepository() }
    createDatabase(
        schemaName = System.getenv("BOOKK_ME_DB_SCHEME"),
        driver = System.getenv("BOOKK_ME_DB_DRIVER"),
        dbUrl = System.getenv("BOOKK_ME_DB_URL"),
        dbPort = System.getenv("BOOKK_ME_DB_PORT"),
        dbUsername = System.getenv("BOOKK_ME_DB_USER"),
        dbPassword = System.getenv("BOOKK_ME_DB_PASSWORD")
    )
}

internal fun authTables(): Array<Table> = arrayOf(
    AuthDeviceTable,
    AuthenticationTable,
    PasskeyCredentialTable
)