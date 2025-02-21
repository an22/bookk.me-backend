package com.book.user.domain.impl.di

import com.book.user.domain.api.event.UserEventHandler
import com.book.user.domain.api.operation.CreateUser
import com.book.user.domain.api.operation.DeleteUser
import com.book.user.domain.api.operation.EditUser
import com.book.user.domain.api.operation.GetUserByEmail
import com.book.user.domain.api.operation.GetUserById
import com.book.user.domain.impl.event.UserEventHandlerImpl
import com.book.user.domain.impl.operation.CreateUserImpl
import com.book.user.domain.impl.operation.DeleteUserImpl
import com.book.user.domain.impl.operation.EditUserImpl
import com.book.user.domain.impl.operation.GetUserByEmailImpl
import com.book.user.domain.impl.operation.GetUserByIdImpl
import org.koin.dsl.module

fun userDomainModule() = module {
    single<GetUserById> { GetUserByIdImpl(get()) }
    single<CreateUser> { CreateUserImpl(get()) }
    single<DeleteUser> { DeleteUserImpl(get()) }
    single<EditUser> { EditUserImpl(get()) }
    single<GetUserByEmail> { GetUserByEmailImpl(get()) }
    single<UserEventHandler> { UserEventHandlerImpl(get(), get()) }
}