package com.bookk.server.user.client

import com.book.user.domain.api.operation.CreateUser
import com.book.user.domain.api.operation.DeleteUser
import com.book.user.domain.api.operation.GetCurrentUser
import com.book.user.domain.api.operation.IsUserExistWithParameters

interface UserClient {
    val getCurrentUserOperation: GetCurrentUser
    val isUserExistWithParameters: IsUserExistWithParameters
    val createUser: CreateUser
    val deleteUser: DeleteUser
}