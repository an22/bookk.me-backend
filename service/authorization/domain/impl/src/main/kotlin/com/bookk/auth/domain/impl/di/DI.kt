package com.bookk.auth.domain.impl.di

import com.bookk.auth.domain.api.authentication.operation.FinishAssertion
import com.bookk.auth.domain.api.authentication.operation.SignIn
import com.bookk.auth.domain.api.authentication.operation.StartAssertion
import com.bookk.auth.domain.api.delete_account.operation.DeleteAccount
import com.bookk.auth.domain.api.identification.operation.DeletePasskey
import com.bookk.auth.domain.api.identification.operation.GetAttachPasskeyToAccountChallenge
import com.bookk.auth.domain.api.identification.operation.GetAvailablePasskeys
import com.bookk.auth.domain.api.registration.operation.AttachNewPasskeyToAccount
import com.bookk.auth.domain.api.registration.operation.FinishPasskeyRegistration
import com.bookk.auth.domain.api.registration.operation.FinishRegistration
import com.bookk.auth.domain.api.registration.operation.StartPasskeyRegistration
import com.bookk.auth.domain.api.registration.operation.StartRegistration
import com.bookk.auth.domain.api.signout.operation.SignOut
import com.bookk.auth.domain.api.token.operation.GenerateAuthToken
import com.bookk.auth.domain.api.token.operation.RefreshToken
import com.bookk.auth.domain.impl.operation.DeleteAccountImpl
import com.bookk.auth.domain.impl.operation.SignOutImpl
import com.bookk.auth.domain.impl.operation.authentication.FinishAssertionImpl
import com.bookk.auth.domain.impl.operation.authentication.SignInImpl
import com.bookk.auth.domain.impl.operation.authentication.StartAssertionImpl
import com.bookk.auth.domain.impl.operation.identification.DeletePasskeyImpl
import com.bookk.auth.domain.impl.operation.identification.GetAttachPasskeyToAccountChallengeImpl
import com.bookk.auth.domain.impl.operation.identification.GetAvailablePasskeysImpl
import com.bookk.auth.domain.impl.operation.registration.AttachNewPasskeyToAccountImpl
import com.bookk.auth.domain.impl.operation.registration.FinishPasskeyRegistrationImpl
import com.bookk.auth.domain.impl.operation.registration.FinishRegistrationImpl
import com.bookk.auth.domain.impl.operation.registration.StartPasskeyRegistrationImpl
import com.bookk.auth.domain.impl.operation.registration.StartRegistrationImpl
import com.bookk.auth.domain.impl.operation.token.GenerateAuthTokenImpl
import com.bookk.auth.domain.impl.operation.token.RefreshTokenImpl
import com.bookk.core.AppLevelConstants
import com.bookk.server.user.client.di.userClientModule
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

fun authDomainModule() = module {
    includes(userClientModule(AppLevelConstants.serviceName))
    single<GenerateAuthToken> { GenerateAuthTokenImpl(AppLevelConstants.DOMAIN_NAME, get(), get(), get()) }
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