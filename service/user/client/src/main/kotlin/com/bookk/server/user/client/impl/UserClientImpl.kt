package com.bookk.server.user.client.impl

import com.book.user.domain.api.operation.CreateUser
import com.book.user.domain.api.operation.DeleteUser
import com.book.user.domain.api.operation.GetUserByEmail
import com.book.user.domain.api.operation.GetUserById
import com.bookk.server.user.client.UserClient

internal class UserClientImpl(
    override val getUserById: GetUserById,
    override val getUserByEmail: GetUserByEmail,
    override val createUser: CreateUser,
    override val deleteUser: DeleteUser
) : UserClient