package com.book.auth.domain.impl.di

import com.book.auth.domain.api.operation.DeleteAccount
import com.book.auth.domain.api.operation.FinishRegistration
import com.book.auth.domain.api.operation.GenerateAuthToken
import com.book.auth.domain.api.operation.RefreshToken
import com.book.auth.domain.api.operation.SignOut
import com.book.auth.domain.api.operation.StartRegistration
import com.book.auth.domain.impl.operation.DeleteAccountImpl
import com.book.auth.domain.impl.operation.SignOutImpl
import com.book.auth.domain.impl.operation.registration.FinishRegistrationImpl
import com.book.auth.domain.impl.operation.registration.StartRegistrationImpl
import com.book.auth.domain.impl.operation.token.GenerateAuthTokenImpl
import com.book.auth.domain.impl.operation.token.RefreshTokenImpl
import com.bookk.core.AppLevelConstants
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

fun authDomainModule() = module {
    single<GenerateAuthToken> { GenerateAuthTokenImpl(AppLevelConstants.DOMAIN_NAME, get(), get()) }
    singleOf(::RefreshTokenImpl) bind RefreshToken::class
    singleOf(::SignOutImpl) bind SignOut::class
    singleOf(::DeleteAccountImpl) bind DeleteAccount::class
    singleOf(::StartRegistrationImpl) bind StartRegistration::class
    singleOf(::FinishRegistrationImpl) bind FinishRegistration::class
}