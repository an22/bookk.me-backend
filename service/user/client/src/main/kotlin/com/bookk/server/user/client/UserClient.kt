package com.bookk.server.user.client

import com.bookk.server.user.client.api.CreateUserRequest
import com.bookk.server.user.client.api.UserSnapshot

interface UserClient {
    suspend fun getUserById(userId: Long): Result<UserSnapshot>
    suspend fun getUserByEmail(email: String): Result<UserSnapshot>
    suspend fun createUser(request: CreateUserRequest): Result<Long>
    suspend fun deleteUser(userId: Long): Result<Unit>
}