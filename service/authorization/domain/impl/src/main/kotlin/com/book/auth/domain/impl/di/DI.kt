package com.book.auth.domain.impl.di

import com.book.auth.domain.api.operation.*
import com.book.auth.domain.impl.operation.*
import org.koin.dsl.module

fun authDomainModule() = module {
    single<GenerateAuthToken> { GenerateAuthTokenImpl(get(DIQualifier.DOMAIN_NAME), get(), get()) }
    single<CreateUserAccount> { CreateUserAccountImpl(get(), get()) }
    single<RefreshToken> { RefreshTokenImpl(get(), get()) }
    single<SignOut> { SignOutImpl(get()) }
    single<DeleteAccount> { DeleteAccountImpl(get(), get()) }
}