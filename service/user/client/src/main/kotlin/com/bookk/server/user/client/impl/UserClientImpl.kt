package com.bookk.server.user.client.impl

import com.book.user.domain.api.operation.CreateUser
import com.book.user.domain.api.operation.DeleteUser
import com.book.user.domain.api.operation.GetUserById
import com.book.user.domain.api.operation.IsUserExistWithParameters
import com.bookk.server.user.client.UserClient

internal class UserClientImpl(
    override val getUserByIdOperation: GetUserById,
    override val createUser: CreateUser,
    override val isUserExistWithParameters: IsUserExistWithParameters,
    override val deleteUser: DeleteUser
) : UserClient