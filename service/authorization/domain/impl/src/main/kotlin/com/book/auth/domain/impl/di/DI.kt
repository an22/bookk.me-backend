package com.book.auth.domain.impl.di

import com.book.auth.domain.api.operation.CreateUserAccount
import com.book.auth.domain.api.operation.DeleteAccount
import com.book.auth.domain.api.operation.FinishRegistration
import com.book.auth.domain.api.operation.GenerateAuthToken
import com.book.auth.domain.api.operation.RefreshToken
import com.book.auth.domain.api.operation.SignOut
import com.book.auth.domain.api.operation.StartRegistration
import com.book.auth.domain.impl.operation.CreateUserAccountImpl
import com.book.auth.domain.impl.operation.DeleteAccountImpl
import com.book.auth.domain.impl.operation.FinishRegistrationImpl
import com.book.auth.domain.impl.operation.GenerateAuthTokenImpl
import com.book.auth.domain.impl.operation.RefreshTokenImpl
import com.book.auth.domain.impl.operation.SignOutImpl
import com.book.auth.domain.impl.operation.StartRegistrationImpl
import com.bookk.core.AppLevelConstants
import org.koin.dsl.module

fun authDomainModule() = module {
    single<GenerateAuthToken> { GenerateAuthTokenImpl(AppLevelConstants.DOMAIN_NAME, get(), get()) }
    single<CreateUserAccount> { CreateUserAccountImpl(get(), get()) }
    single<RefreshToken> { RefreshTokenImpl(get(), get()) }
    single<SignOut> { SignOutImpl(get()) }
    single<DeleteAccount> { DeleteAccountImpl(get(), get()) }
    single<StartRegistration> { StartRegistrationImpl(get(), get(), get()) }
    single<FinishRegistration> { FinishRegistrationImpl(get(), get()) }
}