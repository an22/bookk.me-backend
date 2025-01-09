package com.book.user.domain.impl.di

import com.book.user.domain.api.operation.CreateUser
import com.book.user.domain.api.operation.DeleteUser
import com.book.user.domain.api.operation.GetUserById
import com.book.user.domain.impl.operation.CreateUserImpl
import com.book.user.domain.impl.operation.DeleteUserImpl
import com.book.user.domain.impl.operation.GetUserByIdImpl
import org.koin.dsl.module

fun userDomainModule() = module {
    single<GetUserById> { GetUserByIdImpl(get()) }
    single<CreateUser> { CreateUserImpl(get()) }
    single<DeleteUser> { DeleteUserImpl(get()) }
}