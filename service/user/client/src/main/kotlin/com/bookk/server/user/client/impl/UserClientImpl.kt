package com.bookk.server.user.client.impl

import com.book.user.domain.api.entity.EmailBody
import com.book.user.domain.api.entity.User
import com.book.user.domain.api.operation.CreateUser
import com.book.user.domain.api.operation.DeleteUser
import com.book.user.domain.api.operation.GetUserByEmail
import com.book.user.domain.api.operation.GetUserById
import com.bookk.server.user.client.UserClient
import com.bookk.server.user.client.api.CreateUserRequest
import com.bookk.server.user.client.api.UserSnapshot

internal class UserClientImpl(
    private val getUserById: GetUserById,
    private val getUserByEmail: GetUserByEmail,
    private val createUser: CreateUser,
    private val deleteUser: DeleteUser
) : UserClient {
    override suspend fun getUserById(userId: Long): Result<UserSnapshot> {
        return getUserById.invoke(userId)
            .map { UserSnapshot.fromUser(it) }
    }

    override suspend fun getUserByEmail(email: String): Result<UserSnapshot> {
        return getUserByEmail.invoke(EmailBody(email))
            .map { UserSnapshot.fromUser(it) }
    }

    override suspend fun createUser(request: CreateUserRequest): Result<Long> {
        return createUser.invoke(
            user = User(
                id = 0,
                name = request.name,
                email = request.email,
                lastName = request.lastName
            )
        ).map { it.id }
    }

    override suspend fun deleteUser(userId: Long): Result<Unit> {
        return deleteUser.invoke(userId)
    }


}