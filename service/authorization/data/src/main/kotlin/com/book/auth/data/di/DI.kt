package com.book.auth.data.di

import com.book.auth.data.datasource.AccountDataSourceImpl
import com.book.auth.data.datasource.DeviceDataSourceImpl
import com.book.auth.data.datasource.PassKeyDataSourceImpl
import com.book.auth.data.datasource.YubicoCredentialRepository
import com.book.auth.domain.datasource.AccountDataSource
import com.book.auth.domain.datasource.DeviceDataSource
import com.book.auth.domain.datasource.PassKeyDataSource
import com.book.auth.domain.repository.CacheableCredentialRepository
import com.book.core.data.database.createDatabase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

fun authDataModule() = module {
    singleOf(::AccountDataSourceImpl) bind AccountDataSource::class
    singleOf(::DeviceDataSourceImpl) bind DeviceDataSource::class
    singleOf(::PassKeyDataSourceImpl) bind PassKeyDataSource::class
    factoryOf(::YubicoCredentialRepository) bind CacheableCredentialRepository::class
    createDatabase()
}