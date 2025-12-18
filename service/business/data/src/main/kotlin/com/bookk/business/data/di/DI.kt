package com.bookk.business.data.di

import com.bookk.business.data.datasource.BusinessDataSourceImpl
import com.bookk.business.domain.datasource.BusinessDataSource
import com.bookk.core.data.database.createDatabase
import org.koin.dsl.module

fun businessDataModule() = module {
    single<BusinessDataSource> { BusinessDataSourceImpl() }
    createDatabase()
}