package com.bookk.business.data.di

import com.bookk.business.data.datasource.BusinessDataSourceImpl
import com.bookk.business.data.datasource.ClientDataSourceImpl
import com.bookk.business.data.datasource.EmployeeDataSourceImpl
import com.bookk.business.data.datasource.EmployeeInvitationDataSourceImpl
import com.bookk.business.data.datasource.ServiceDataSourceImpl
import com.bookk.business.domain.datasource.BusinessDataSource
import com.bookk.business.domain.datasource.ClientDataSource
import com.bookk.business.domain.datasource.EmployeeDataSource
import com.bookk.business.domain.datasource.EmployeeInvitationDataSource
import com.bookk.business.domain.datasource.ServiceDataSource
import com.bookk.core.data.database.createDatabase
import org.koin.dsl.module

fun businessDataModule() = module {
    single<BusinessDataSource> { BusinessDataSourceImpl() }
    single<ClientDataSource> { ClientDataSourceImpl() }
    single<ServiceDataSource> { ServiceDataSourceImpl() }
    single<EmployeeDataSource> { EmployeeDataSourceImpl() }
    single<EmployeeInvitationDataSource> { EmployeeInvitationDataSourceImpl() }
    createDatabase()
}