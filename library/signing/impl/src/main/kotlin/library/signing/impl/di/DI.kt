package library.signing.impl.di

import library.signing.GetActiveSigningKey
import library.signing.GetVerificationKeys
import library.signing.RotateSigningKeys
import library.signing.SigningKeyDataSource
import library.signing.impl.GetActiveSigningKeyImpl
import library.signing.impl.GetVerificationKeysImpl
import library.signing.impl.RotateSigningKeysImpl
import library.signing.impl.SigningKeyDataSourceImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

fun signingModule() = module {
    singleOf(::SigningKeyDataSourceImpl) bind SigningKeyDataSource::class
    singleOf(::GetActiveSigningKeyImpl) bind GetActiveSigningKey::class
    singleOf(::GetVerificationKeysImpl) bind GetVerificationKeys::class
    singleOf(::RotateSigningKeysImpl) bind RotateSigningKeys::class
}
