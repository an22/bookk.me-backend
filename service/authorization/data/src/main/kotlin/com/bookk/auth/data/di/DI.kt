package com.bookk.auth.data.di

import com.bookk.auth.data.datasource.AccountDataSourceImpl
import com.bookk.auth.data.datasource.DeviceDataSourceImpl
import com.bookk.auth.data.datasource.PassKeyDataSourceImpl
import com.bookk.auth.data.datasource.SigningKeyDataSourceImpl
import com.bookk.auth.data.datasource.YubicoCredentialRepository
import com.bookk.auth.domain.datasource.AccountDataSource
import com.bookk.auth.domain.datasource.DeviceDataSource
import com.bookk.auth.domain.datasource.PassKeyDataSource
import com.bookk.auth.domain.datasource.SigningKeyDataSource
import com.bookk.auth.domain.repository.CacheableCredentialRepository
import com.bookk.core.data.database.createDatabase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

fun authDataModule() = module {
    singleOf(::AccountDataSourceImpl) bind AccountDataSource::class
    singleOf(::DeviceDataSourceImpl) bind DeviceDataSource::class
    singleOf(::PassKeyDataSourceImpl) bind PassKeyDataSource::class
    singleOf(::SigningKeyDataSourceImpl) bind SigningKeyDataSource::class
    factoryOf(::YubicoCredentialRepository) bind CacheableCredentialRepository::class
    createDatabase()
}