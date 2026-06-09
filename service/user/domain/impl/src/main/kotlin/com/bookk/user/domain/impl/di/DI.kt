package com.bookk.user.domain.impl.di

import com.bookk.core.data.eventstreaming.EventHandler
import com.bookk.user.domain.api.operation.CreateContactForm
import com.bookk.user.domain.api.operation.CreateUser
import com.bookk.user.domain.api.operation.DeleteUser
import com.bookk.user.domain.api.operation.EditUser
import com.bookk.user.domain.api.operation.GetUserByEmail
import com.bookk.user.domain.api.operation.GetUserById
import com.bookk.user.domain.impl.event.UserEventHandler
import com.bookk.user.domain.impl.operation.CreateContactFormImpl
import com.bookk.user.domain.impl.operation.CreateUserImpl
import com.bookk.user.domain.impl.operation.DeleteUserImpl
import com.bookk.user.domain.impl.operation.EditUserImpl
import com.bookk.user.domain.impl.operation.GetUserByEmailImpl
import com.bookk.user.domain.impl.operation.GetUserByIdImpl
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

fun userDomainModule() = module {
    singleOf(::GetUserByIdImpl) bind GetUserById::class
    singleOf(::CreateUserImpl) bind CreateUser::class
    singleOf(::DeleteUserImpl) bind DeleteUser::class
    singleOf(::EditUserImpl) bind EditUser::class
    singleOf(::CreateContactFormImpl) bind CreateContactForm::class
    singleOf(::GetUserByEmailImpl) bind GetUserByEmail::class
    singleOf(::GetUserByIdImpl) bind GetUserById::class
    factoryOf(::UserEventHandler) bind EventHandler::class
}