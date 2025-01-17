package com.book.auth.data.di

import com.book.auth.data.datasource.AccountDataSourceImpl
import com.book.auth.data.datasource.DeviceDataSourceImpl
import com.book.auth.data.datasource.PassKeyDataSourceImpl
import com.book.auth.data.datasource.YubicoCredentialRepository
import com.book.auth.domain.datasource.AccountDataSource
import com.book.auth.domain.datasource.DeviceDataSource
import com.book.auth.domain.datasource.PassKeyDataSource
import com.book.core.data.database.createDatabase
import com.bookk.server.user.client.di.userClientModule
import com.yubico.webauthn.CredentialRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

fun authDataModule() = module {
    includes(userClientModule(System.getenv("BOOKK_ME_SERVICE_NAME")))
    singleOf(::AccountDataSourceImpl) bind AccountDataSource::class
    singleOf(::DeviceDataSourceImpl) bind DeviceDataSource::class
    singleOf(::PassKeyDataSourceImpl) bind PassKeyDataSource::class
    singleOf(::YubicoCredentialRepository) bind CredentialRepository::class
    createDatabase(
        schemaName = System.getenv("BOOKK_ME_DB_SCHEME"),
        driver = System.getenv("BOOKK_ME_DB_DRIVER"),
        dbUrl = System.getenv("BOOKK_ME_DB_URL"),
        dbPort = System.getenv("BOOKK_ME_DB_PORT"),
        dbUsername = System.getenv("BOOKK_ME_DB_USER"),
        dbPassword = System.getenv("BOOKK_ME_DB_PASSWORD")
    )
}