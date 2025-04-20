package com.book.business.domain.impl.di

import com.book.business.domain.api.operation.CreateBusiness
import com.book.business.domain.api.operation.GetBusinessById
import com.book.business.domain.impl.operation.CreateBusinessImpl
import com.book.business.domain.impl.operation.GetBusinessByIdImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

fun businessDomainModule() = module {
    singleOf(::GetBusinessByIdImpl) bind GetBusinessById::class
    singleOf(::CreateBusinessImpl) bind CreateBusiness::class
}