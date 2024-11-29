package com.book.auth.domain.impl.operation

import com.book.auth.domain.api.datasource.UserAuthLocalDataSource
import com.book.auth.domain.api.operation.DeleteAccount
import com.book.auth.domain.api.operation.DeleteAccount.DeleteAccountError.InvalidCredentials
import com.book.auth.domain.impl.totp.createTotpConfig
import com.book.user.domain.api.operation.DeleteUser
import com.book.user.domain.api.util.createPasswordHash
import com.bookk.server.user.client.UserClient
import dev.turingcomplete.kotlinonetimepassword.TimeBasedOneTimePasswordGenerator
import org.apache.commons.codec.binary.Base32

internal class DeleteAccountImpl(
    private val authLocalDataSource: UserAuthLocalDataSource,
    private val userClient: UserClient
) : DeleteAccount {

    private val totpConfig = createTotpConfig()
    private val base32 = Base32()

    override suspend fun call(params: DeleteAccount.Param): Result<Unit> = runCatching {
        val authRecord = authLocalDataSource.getAuthRecordByUsername(params.userName) ?: throw InvalidCredentials
        val generator = TimeBasedOneTimePasswordGenerator(
            secret = base32.decode(authRecord.totpSecret),
            config = totpConfig
        )
        val isCredentialsInvalid = authRecord.passwordHash != createPasswordHash(params.info.password) &&
                !generator.isValid(params.info.totpCode)
        if (isCredentialsInvalid) throw InvalidCredentials
        authLocalDataSource.deleteAccount(authRecord.userId)
        userClient.deleteUser.call(DeleteUser.Param(authRecord.userId))
    }

    override suspend fun a() = userClient.deleteUser.call(DeleteUser.Param(1))
}