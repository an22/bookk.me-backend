package com.book.business.data.di

import com.book.business.data.datasource.BusinessDataSourceImpl
import com.book.business.domain.datasource.BusinessDataSource
import com.book.core.data.database.createDatabase
import org.koin.dsl.module

fun businessDataModule() = module {
    single<BusinessDataSource> { BusinessDataSourceImpl() }
    createDatabase()
}