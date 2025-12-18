package com.bookk.user.domain.datasource

import com.bookk.user.domain.api.entity.User
import com.bookk.user.domain.api.entity.UserEditModel
import kotlin.uuid.Uuid

interface UserDataSource {
    suspend fun insertNewUser(user: User): User
    suspend fun updateUser(id: Uuid, user: UserEditModel): Boolean
    suspend fun getUserById(id: Uuid): User?
    suspend fun getUserByEmail(email: String): User?
    suspend fun deleteUser(id: Uuid)
}