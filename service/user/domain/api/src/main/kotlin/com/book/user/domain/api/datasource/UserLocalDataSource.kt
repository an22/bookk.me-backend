package com.book.user.domain.api.datasource

import com.book.user.domain.api.entity.User

interface UserLocalDataSource {
    suspend fun insertNewUser(user: User): Long
    suspend fun getUserById(id: Long): User?
    suspend fun getUserByPhoneOrEmail(phone: String, email: String): User?
}