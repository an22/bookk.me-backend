package com.book.auth.domain.impl.operation.registration

import com.book.auth.domain.api.registration.entity.CreateAccountRequest
import com.book.auth.domain.api.registration.operation.StartPasskeyRegistration
import com.book.auth.domain.api.registration.operation.StartRegistration
import com.book.auth.domain.api.registration.operation.StartRegistration.Error.EmailAlreadyExist
import com.book.auth.domain.api.registration.operation.StartRegistration.Error.InvalidEmailFormat
import com.book.core.domain.entity.BusinessError
import com.book.core.domain.entity.throwIf
import com.bookk.core.newRandomUUIDByteArray
import com.bookk.server.user.client.UserClient
import io.ktor.http.HttpStatusCode

internal class StartRegistrationImpl(
    private val userClient: UserClient,
    private val startPasskeyRegistration: StartPasskeyRegistration
) : StartRegistration {

    private val emailRegex = Regex(RegistrationConstants.EMAIL_REGEX)

    override suspend fun invoke(request: CreateAccountRequest) = runCatching {
        if (!emailRegex.matches(request.email)) throw InvalidEmailFormat
        userClient.getUserByEmail(request.email)
            .onSuccess { throw EmailAlreadyExist }
            //TODO Very questionable check. Rework
            .throwIf { (it as? BusinessError)?.statusCode != HttpStatusCode.NotFound.value }
        startPasskeyRegistration(
            userHandle = newRandomUUIDByteArray(),
            passkeyDisplayName = "${request.firstName} ${request.lastName}"
        ).getOrThrow()
    }
}