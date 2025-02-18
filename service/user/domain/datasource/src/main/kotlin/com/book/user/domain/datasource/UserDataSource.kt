package com.book.user.domain.datasource

import com.book.user.domain.api.entity.User
import com.book.user.domain.api.entity.UserEditModel

interface UserDataSource {
    suspend fun insertNewUser(user: User): User
    suspend fun updateUser(id: Long, user: UserEditModel): Boolean
    suspend fun getUserById(id: Long): User?
    suspend fun getUserByEmail(email: String): User?
    suspend fun deleteUser(id: Long)
}