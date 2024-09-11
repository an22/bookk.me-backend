package com.bookk.server.user.client.di

import com.bookk.server.user.client.UserClient
import com.bookk.server.user.client.impl.LocalUserClient
import org.koin.dsl.module

fun userClientModule() = module {
    single<UserClient> { LocalUserClient(get(), get(), get()) }
}