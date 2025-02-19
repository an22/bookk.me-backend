package com.book.auth.domain.impl.di

import com.book.auth.domain.api.authentication.operation.FinishAssertion
import com.book.auth.domain.api.authentication.operation.SignIn
import com.book.auth.domain.api.authentication.operation.StartAssertion
import com.book.auth.domain.api.delete_account.operation.DeleteAccount
import com.book.auth.domain.api.registration.operation.FinishRegistration
import com.book.auth.domain.api.registration.operation.StartRegistration
import com.book.auth.domain.api.signout.operation.SignOut
import com.book.auth.domain.api.token.operation.GenerateAuthToken
import com.book.auth.domain.api.token.operation.RefreshToken
import com.book.auth.domain.impl.operation.DeleteAccountImpl
import com.book.auth.domain.impl.operation.SignOutImpl
import com.book.auth.domain.impl.operation.authentication.FinishAssertionImpl
import com.book.auth.domain.impl.operation.authentication.SignInImpl
import com.book.auth.domain.impl.operation.authentication.StartAssertionImpl
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
    singleOf(::StartAssertionImpl) bind StartAssertion::class
    singleOf(::FinishAssertionImpl) bind FinishAssertion::class
    singleOf(::SignInImpl) bind SignIn::class
}