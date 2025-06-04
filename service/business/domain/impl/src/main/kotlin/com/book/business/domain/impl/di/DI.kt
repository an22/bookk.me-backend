package com.book.business.domain.impl.di

import com.book.business.domain.api.operation.CreateBusiness
import com.book.business.domain.api.operation.DeleteBusiness
import com.book.business.domain.api.operation.GetBusinessById
import com.book.business.domain.api.operation.GetDashboardBusiness
import com.book.business.domain.api.operation.UpdateBusiness
import com.book.business.domain.impl.event.BusinessEventHandlerImpl
import com.book.business.domain.impl.operation.CreateBusinessImpl
import com.book.business.domain.impl.operation.DeleteBusinessImpl
import com.book.business.domain.impl.operation.GetBusinessByIdImpl
import com.book.business.domain.impl.operation.GetDashboardBusinessImpl
import com.book.business.domain.impl.operation.UpdateBusinessImpl
import com.book.core.data.eventstreaming.EventHandler
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
    factoryOf(::BusinessEventHandlerImpl) bind EventHandler::class
}