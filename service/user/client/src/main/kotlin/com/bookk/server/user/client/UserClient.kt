package com.bookk.server.user.client

import com.book.user.domain.api.operation.CreateUser
import com.book.user.domain.api.operation.DeleteUser
import com.book.user.domain.api.operation.GetUserById
import com.book.user.domain.api.operation.IsUserExistWithParameters

interface UserClient {
    val getUserByIdOperation: GetUserById
    val isUserExistWithParameters: IsUserExistWithParameters
    val createUser: CreateUser
    val deleteUser: DeleteUser
}