package com.book.user.domain.datasource

import com.book.user.domain.api.entity.User

interface UserDataSource {
    suspend fun insertNewUser(user: User): User
    suspend fun getUserById(id: Long): User?
    suspend fun deleteUser(id: Long)
}