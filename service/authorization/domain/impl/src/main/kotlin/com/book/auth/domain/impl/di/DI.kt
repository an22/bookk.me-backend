package com.book.auth.domain.impl.di

import com.book.auth.domain.api.authentication.operation.FinishAssertion
import com.book.auth.domain.api.authentication.operation.SignIn
import com.book.auth.domain.api.authentication.operation.StartAssertion
import com.book.auth.domain.api.delete_account.operation.DeleteAccount
import com.book.auth.domain.api.identification.operation.DeletePasskey
import com.book.auth.domain.api.identification.operation.GetAttachPasskeyToAccountChallenge
import com.book.auth.domain.api.identification.operation.GetAvailablePasskeys
import com.book.auth.domain.api.registration.operation.AttachNewPasskeyToAccount
import com.book.auth.domain.api.registration.operation.FinishPasskeyRegistration
import com.book.auth.domain.api.registration.operation.FinishRegistration
import com.book.auth.domain.api.registration.operation.StartPasskeyRegistration
import com.book.auth.domain.api.registration.operation.StartRegistration
import com.book.auth.domain.api.signout.operation.SignOut
import com.book.auth.domain.api.token.operation.GenerateAuthToken
import com.book.auth.domain.api.token.operation.RefreshToken
import com.book.auth.domain.impl.operation.DeleteAccountImpl
import com.book.auth.domain.impl.operation.SignOutImpl
import com.book.auth.domain.impl.operation.authentication.FinishAssertionImpl
import com.book.auth.domain.impl.operation.authentication.SignInImpl
import com.book.auth.domain.impl.operation.authentication.StartAssertionImpl
import com.book.auth.domain.impl.operation.identification.DeletePasskeyImpl
import com.book.auth.domain.impl.operation.identification.GetAttachPasskeyToAccountChallengeImpl
import com.book.auth.domain.impl.operation.identification.GetAvailablePasskeysImpl
import com.book.auth.domain.impl.operation.registration.AttachNewPasskeyToAccountImpl
import com.book.auth.domain.impl.operation.registration.FinishPasskeyRegistrationImpl
import com.book.auth.domain.impl.operation.registration.FinishRegistrationImpl
import com.book.auth.domain.impl.operation.registration.StartPasskeyRegistrationImpl
import com.book.auth.domain.impl.operation.registration.StartRegistrationImpl
import com.book.auth.domain.impl.operation.token.GenerateAuthTokenImpl
import com.book.auth.domain.impl.operation.token.RefreshTokenImpl
import com.bookk.core.AppLevelConstants
import com.bookk.server.business.client.di.businessClientModule
import com.bookk.server.user.client.di.userClientModule
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

fun authDomainModule() = module {
    includes(userClientModule(AppLevelConstants.serviceName))
    includes(businessClientModule(AppLevelConstants.serviceName))
    single<GenerateAuthToken> { GenerateAuthTokenImpl(AppLevelConstants.DOMAIN_NAME, get(), get()) }
    singleOf(::RefreshTokenImpl) bind RefreshToken::class
    singleOf(::SignOutImpl) bind SignOut::class
    singleOf(::DeleteAccountImpl) bind DeleteAccount::class
    singleOf(::StartRegistrationImpl) bind StartRegistration::class
    singleOf(::FinishRegistrationImpl) bind FinishRegistration::class
    singleOf(::StartAssertionImpl) bind StartAssertion::class
    singleOf(::FinishAssertionImpl) bind FinishAssertion::class
    singleOf(::SignInImpl) bind SignIn::class
    singleOf(::GetAvailablePasskeysImpl) bind GetAvailablePasskeys::class
    singleOf(::DeletePasskeyImpl) bind DeletePasskey::class
    singleOf(::GetAttachPasskeyToAccountChallengeImpl) bind GetAttachPasskeyToAccountChallenge::class
    singleOf(::StartPasskeyRegistrationImpl) bind StartPasskeyRegistration::class
    singleOf(::FinishPasskeyRegistrationImpl) bind FinishPasskeyRegistration::class
    singleOf(::AttachNewPasskeyToAccountImpl) bind AttachNewPasskeyToAccount::class
}