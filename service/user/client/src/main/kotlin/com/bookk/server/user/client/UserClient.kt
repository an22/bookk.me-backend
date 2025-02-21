package com.bookk.server.user.client

import com.book.user.domain.api.operation.CreateUser
import com.book.user.domain.api.operation.DeleteUser
import com.book.user.domain.api.operation.GetUserByEmail
import com.book.user.domain.api.operation.GetUserById

interface UserClient {
    val getUserById: GetUserById
    val getUserByEmail: GetUserByEmail
    val createUser: CreateUser
    val deleteUser: DeleteUser
}