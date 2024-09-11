package com.book.user.domain.impl.di

import com.book.user.domain.api.operation.CreateUser
import com.book.user.domain.api.operation.GetCurrentUser
import com.book.user.domain.api.operation.IsUserExistWithParameters
import com.book.user.domain.impl.operation.CreateUserImpl
import com.book.user.domain.impl.operation.GetCurrentUserImpl
import com.book.user.domain.impl.operation.IsUserExistWithParametersImpl
import org.koin.dsl.module

fun userDomainModule() = module {
    single<GetCurrentUser> { GetCurrentUserImpl(get()) }
    single<CreateUser> { CreateUserImpl(get()) }
    single<IsUserExistWithParameters> { IsUserExistWithParametersImpl(get()) }
}