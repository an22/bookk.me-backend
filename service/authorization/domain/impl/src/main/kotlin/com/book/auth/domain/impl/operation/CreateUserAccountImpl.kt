package com.book.auth.domain.impl.operation

import com.book.auth.domain.api.datasource.UserAuthLocalDataSource
import com.book.auth.domain.api.entity.SignUpInfo
import com.book.auth.domain.api.entity.TotpSecret
import com.book.auth.domain.api.operation.CreateUserAccount
import com.book.auth.domain.api.operation.CreateUserAccount.CreateUserAccountError
import com.book.user.domain.api.entity.User
import com.book.user.domain.api.operation.IsUserExistWithParameters.Param
import com.book.user.domain.api.util.createPasswordHash
import com.bookk.server.user.client.UserClient
import dev.turingcomplete.kotlinonetimepassword.HmacAlgorithm
import dev.turingcomplete.kotlinonetimepassword.RandomSecretGenerator
import org.apache.commons.codec.binary.Base32

const val MIN_PASSWORD_LENGTH = 8

internal class CreateUserAccountImpl(
    private val userClient: UserClient,
    private val localDataSource: UserAuthLocalDataSource
) : CreateUserAccount {

    private val secretGenerator = RandomSecretGenerator()
    private val base32 = Base32()

    override suspend fun call(params: SignUpInfo): Result<TotpSecret> = runCatching {
        if (params.password.length < MIN_PASSWORD_LENGTH) throw CreateUserAccountError.PasswordTooShort
        if (!PASSWORD_REGEX.matches(params.password)) throw CreateUserAccountError.PasswordTooWeak
        val userRecord = localDataSource.getAuthRecordByUsername(params.login)
        if (userRecord != null) throw CreateUserAccountError.LoginAlreadyExist
        val userByParams = userClient.isUserExistWithParameters.call(Param(params.phone, params.email))
        if (userByParams.getOrThrow()) throw CreateUserAccountError.EmailOrPhoneAlreadyExist
        val passwordHash = createPasswordHash(params.password)
        userClient.createUser.call(params.asUser())
            .map {
                val newSecret = secretGenerator.createRandomSecret(HmacAlgorithm.SHA1)
                val encodedSecret = base32.encodeAsString(newSecret)
                localDataSource.createAuthRecord(it, passwordHash, encodedSecret, params)
                TotpSecret(encodedSecret)
            }.getOrThrow()
    }

    private fun SignUpInfo.asUser(): User {
        return User(
            id = -1,
            name = name,
            lastName = lastName,
            phone = phone,
            email = email,
            role = role
        )
    }

    companion object {
        private val PASSWORD_REGEX = Regex("^(?=.*[A-Z])(?=.*[!@#$&*])(?=.*[0-9])(?=.*[a-z]).{8,64}$")
    }
}