package com.bookk.business.domain.impl.di

import com.bookk.business.domain.api.operation.CreateBusiness
import com.bookk.business.domain.api.operation.DeleteBusiness
import com.bookk.business.domain.api.operation.GetBusinessById
import com.bookk.business.domain.api.operation.GetDashboardBusiness
import com.bookk.business.domain.api.operation.GetUserBusinesses
import com.bookk.business.domain.api.operation.UpdateBusiness
import com.bookk.business.domain.impl.event.BusinessEventHandlerImpl
import com.bookk.business.domain.impl.operation.CreateBusinessImpl
import com.bookk.business.domain.impl.operation.DeleteBusinessImpl
import com.bookk.business.domain.impl.operation.GetBusinessByIdImpl
import com.bookk.business.domain.impl.operation.GetDashboardBusinessImpl
import com.bookk.business.domain.impl.operation.GetUserBusinessesImpl
import com.bookk.business.domain.impl.operation.UpdateBusinessImpl
import com.bookk.core.data.eventstreaming.EventHandler
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

fun businessDomainModule() = module {
    singleOf(::GetBusinessByIdImpl) bind GetBusinessById::class
    singleOf(::CreateBusinessImpl) bind CreateBusiness::class
    singleOf(::DeleteBusinessImpl) bind DeleteBusiness::class
    singleOf(::GetDashboardBusinessImpl) bind GetDashboardBusiness::class
    singleOf(::UpdateBusinessImpl) bind UpdateBusiness::class
    singleOf(::GetUserBusinessesImpl) bind GetUserBusinesses::class
    factoryOf(::BusinessEventHandlerImpl) bind EventHandler::class
}